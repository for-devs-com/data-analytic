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
import java.util.*;
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

    @Override
    public ResponseEntity<String> setDatabaseConnection(DatabaseConnectionRequest request) {
        if (allConnectionFieldsPresent(request)) {
            try {
                if (dataSourceConnectionManager.createAndTestConnection(request)) {
                    this.databaseConnectionRequest = request;
                    log.info("Connected successfully to database: {}", request.getDatabaseName());
                    return ResponseEntity.ok("Connected successfully to database: " + request.getDatabaseName());
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to connect to database: " + request.getDatabaseName());
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
        if (isDatabaseConnectionConfigured()) {
            try {
                List<String> tables = schemaDiscoveryService.listTables(this.databaseConnectionRequest);
                return ResponseEntity.ok(tables);
            } catch (Exception e) {
                return handleExceptionAsListString(e);
            }
        } else {
            return handleMissingCredentialsForList();
        }
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> listColumns(String tableName) {
        if (isDatabaseConnectionConfigured()) {
            try {
                String dataSourceKey = dataSourceConnectionManager.generateHashKey(this.databaseConnectionRequest);
                List<Map<String, Object>> columns = schemaDiscoveryService.listColumns(tableName, this.databaseConnectionRequest.getDatabaseType().toLowerCase(), dataSourceKey);
                return ResponseEntity.ok(columns);
            } catch (SQLException e) {
                return handleExceptionAsListMap(e, "SQL error listing columns for table: " + tableName);
            } catch (Exception e) {
                return handleExceptionAsListMap(e, "Error listing columns for table: " + tableName);
            }
        } else {
            return handleMissingCredentialsForListMap();
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getTableData(String tableName, int page, int size) {
        if (isDatabaseConnectionConfigured()) {
            try {
                DynamicTableData tableData = schemaDiscoveryService.getTableDataWithPagination(tableName, this.databaseConnectionRequest, page, size).getBody();
                Map<String, Object> response = new HashMap<>();
                response.put("rows", Objects.requireNonNull(tableData).getRows());
                response.put("columns", tableData.getColumns());
                response.put("currentPage", page);
                response.put("pageSize", size);
                response.put("totalRows", tableData.getTotalRows());
                response.put("tableName", tableName);

                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return handleExceptionAsMap(e);
            }
        } else {
            return handleMissingCredentialsForMap();
        }
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> executeQuery(String query) {
        if (isDatabaseConnectionConfigured()) {
            if (isValidQuery(query)) {
                try {
                    String dataSourceKey = dataSourceConnectionManager.generateHashKey(this.databaseConnectionRequest);
                    JdbcTemplate jdbcTemplate = dataSourceConnectionManager.getJdbcTemplateForDb(this.databaseConnectionRequest.getDatabaseType().toLowerCase(), dataSourceKey);

                    List<Map<String, Object>> result = jdbcTemplate.query(conn -> conn.prepareStatement(query), new ColumnMapRowMapper());
                    return ResponseEntity.ok(result);
                } catch (Exception e) {
                    return handleExceptionAsListMap(e, "Error executing query: " + query);
                }
            } else {
                log.error("Invalid SQL query: {}", query);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        } else {
            return handleMissingCredentialsForListMap();
        }
    }

    private boolean isDatabaseConnectionConfigured() {
        return this.databaseConnectionRequest != null;
    }

    private <T> ResponseEntity<T> handleMissingCredentials() {
        log.error("Credentials must be set before calling this method.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
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

    private ResponseEntity<List<String>> handleMissingCredentialsForList() {
        log.error("Credentials must be set before calling this method.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
    }

    private ResponseEntity<Map<String, Object>> handleMissingCredentialsForMap() {
        log.error("Credentials must be set before calling this method.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyMap());
    }

    private ResponseEntity<List<Map<String, Object>>> handleMissingCredentialsForListMap() {
        log.error("Credentials must be set before calling this method.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
    }

    private ResponseEntity<List<String>> handleExceptionAsListString(Exception e) {
        log.error("Error listing tables", e);
        if ("prod".equals(environment.getProperty("spring.profiles.active"))) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        } else {
            List<String> errorList = Collections.singletonList("Error listing tables: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorList);
        }
    }

}
