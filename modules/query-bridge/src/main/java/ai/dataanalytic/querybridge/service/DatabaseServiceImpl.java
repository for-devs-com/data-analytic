package ai.dataanalytic.querybridge.service;

import ai.dataanalytic.querybridge.dto.DynamicTableData;
import ai.dataanalytic.sharedlibrary.datasource.database.DataSourceConnectionManager;
import ai.dataanalytic.sharedlibrary.dto.DatabaseConnectionRequest;
import ai.dataanalytic.sharedlibrary.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
public class DatabaseServiceImpl implements DatabaseService {

    @Autowired
    private DataSourceConnectionManager dataSourceConnectionManager;

    @Autowired
    private SchemaDiscoveryService schemaDiscoveryService;

    @Autowired
    private Environment environment;

    private DatabaseConnectionRequest databaseConnectionRequest;

    private static void verifyCredentials() {
        log.error("Credentials must be set before calling this method.");
    }

    @Override
    public ResponseEntity<String> setDatabaseConnection(DatabaseConnectionRequest databaseConnectionRequest) {
        if (allConnectionFieldsPresent(databaseConnectionRequest)) {
            try {
                if (dataSourceConnectionManager.createAndTestConnection(databaseConnectionRequest)) {
                    this.databaseConnectionRequest = databaseConnectionRequest;
                    String dataSourceKey = dataSourceConnectionManager.generateHashKey(databaseConnectionRequest);
                    dataSourceConnectionManager.getJdbcTemplateForDb(databaseConnectionRequest.getDatabaseType().toLowerCase(), dataSourceKey);

                    log.info("Connected successfully to database: {}", databaseConnectionRequest.getDatabaseName());
                    return ResponseEntity.ok("Connected successfully to database: " + databaseConnectionRequest.getDatabaseName());
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to connect to database: " + databaseConnectionRequest.getDatabaseName());
                }
            } catch (Exception e) {
                log.error("Error connecting to the database", e);
                return handleExceptionAsString(e);
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid credentials provided");
        }
    }

    private boolean allConnectionFieldsPresent(DatabaseConnectionRequest request) {
        return StringUtils.allFieldsPresent(
                request.getDatabaseName(),
                request.getHost(),
                request.getUserName(),
                request.getPassword()
        );
    }


    @Override
    public ResponseEntity<List<String>> listTables() {
        if (this.databaseConnectionRequest == null) {
            verifyCredentials();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        try {
            List<String> tables = schemaDiscoveryService.listTables(this.databaseConnectionRequest);
            log.info("Tables: {}", tables);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            log.error("Error listing tables", e);
            return handleExceptionAsListString(e);
        }
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> listColumns(String tableName) {
        if (this.databaseConnectionRequest == null) {
            verifyCredentials();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        try {
            String dataSourceKey = dataSourceConnectionManager.generateHashKey(this.databaseConnectionRequest);
            List<Map<String, Object>> columns = schemaDiscoveryService.listColumns(tableName, this.databaseConnectionRequest.getDatabaseType().toLowerCase(), dataSourceKey);
            return ResponseEntity.ok(columns);
        } catch (SQLException e) {
            log.error("SQL error listing columns for table: {}", tableName, e);
            return handleExceptionAsListMap(e, "SQL error listing columns for table: ");
        } catch (Exception e) {
            log.error("Error listing columns for table: {}", tableName, e);
            return handleExceptionAsListMap(e, "Error listing columns for table: ");
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getTableData(String tableName, int page, int size) {
        if (this.databaseConnectionRequest == null) {
            verifyCredentials();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        try {
            ResponseEntity<DynamicTableData> responseEntity = schemaDiscoveryService.getTableDataWithPagination(tableName, this.databaseConnectionRequest, page, size);
            DynamicTableData tableData = responseEntity.getBody();

            Map<String, Object> response = new HashMap<>();
            response.put("rows", tableData.getRows());
            response.put("columns", tableData.getColumns());
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("totalRows", tableData.getTotalRows());
            response.put("tableName", tableName);

            log.info("Table Response Data: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error obtaining data from table: {}", tableName, e);
            return handleExceptionAsMap(e);
        }
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> executeQuery(String query) {
        if (this.databaseConnectionRequest == null) {
            verifyCredentials();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        if (!isValidQuery(query)) {
            log.error("Invalid SQL query: {}", query);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try {
            log.info("Executing query: {}", query);

            String dataSourceKey = dataSourceConnectionManager.generateHashKey(this.databaseConnectionRequest);
            JdbcTemplate jdbcTemplate = dataSourceConnectionManager.getJdbcTemplateForDb(this.databaseConnectionRequest.getDatabaseType().toLowerCase(), dataSourceKey);

            List<Map<String, Object>> result = jdbcTemplate.query(
                    conn -> conn.prepareStatement(query),
                    new ColumnMapRowMapper()
            );

            log.info("Query executed successfully. Result size: {}", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error executing query: {}. Error: {}", query, e.getMessage(), e);
            return handleExceptionAsListMap(e, "Error executing query: ");
        }
    }

    private boolean isValidQuery(String query) {
        String sqlPattern = "^[a-zA-Z0-9_\\s,=*'();]*$";
        return Pattern.matches(sqlPattern, query);
    }

    private ResponseEntity<List<Map<String, Object>>> handleExceptionAsListMap(Exception e, String message) {
        log.error(message, e);
        if ("prod".equals(environment.getProperty("spring.profiles.active"))) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        } else {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", message + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonList(errorMap));
        }
    }

    private ResponseEntity<Map<String, Object>> handleExceptionAsMap(Exception e) {
        log.error("Error obtaining data from table: ", e);
        if ("prod".equals(environment.getProperty("spring.profiles.active"))) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyMap());
        } else {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Error obtaining data from table: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    private ResponseEntity<String> handleExceptionAsString(Exception e) {
        log.error("Error connecting to the database: ", e);
        if ("prod".equals(environment.getProperty("spring.profiles.active"))) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error connecting to the database: " + e.getMessage());
        }
    }

    private ResponseEntity<List<String>> handleExceptionAsListString(Exception e) {
        log.error("Error listing tables: ", e);
        if ("prod".equals(environment.getProperty("spring.profiles.active"))) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        } else {
            List<String> errorList = Collections.singletonList("Error listing tables: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorList);
        }
    }
}
