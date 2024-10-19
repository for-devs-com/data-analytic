package ai.dataanalytic.querybridge.service;

import ai.dataanalytic.querybridge.config.DynamicDataSourceManager;
import ai.dataanalytic.sharedlibrary.dto.DatabaseConnectionRequest;
import ai.dataanalytic.querybridge.dto.DynamicTableData;
import jakarta.servlet.http.HttpSession;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Implementation of the DatabaseService interface.
 * This class handles the business logic for database operations.
 */
@Slf4j
@Service
public class DatabaseServiceImpl implements DatabaseService {

    @Autowired
    private DynamicDataSourceManager dynamicDataSourceManager;

    @Autowired
    private SchemaDiscoveryService schemaDiscoveryService;

    @Autowired
    private Environment environment;

    private static final String SESSION_ATTRIBUTE_CONNECTION = "dbConnection";

    @Override
    public ResponseEntity<String> setDatabaseConnection(DatabaseConnectionRequest databaseConnectionRequest, HttpSession session) {
        // Validate the provided credentials
        if (!validateCredentials(databaseConnectionRequest)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid credentials provided");
        }

        try {
            // Try to create and test a database connection with the provided credentials.
            JdbcTemplate jdbcTemplate = dynamicDataSourceManager.createAndTestConnection(databaseConnectionRequest);

            String userId = getUserIdFromSession(session);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            if (jdbcTemplate == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to connect to database");
            }

            // Store the JdbcTemplate in a map keyed by userId
            userConnections.put(userId, jdbcTemplate);

            // Store the JdbcTemplate in the session
            session.setAttribute(SESSION_ATTRIBUTE_CONNECTION, jdbcTemplate);

            return ResponseEntity.ok("Connected successfully to database: " + databaseConnectionRequest.getDatabaseName());
        } catch (Exception e) {
            log.error("Error connecting to the database", e);
            return handleException(e, "Error connecting to the database.");
        }
    }

    private String getUserIdFromSession(HttpSession session) {
        return (String) session.getAttribute("userId");
    }



    // Mapa para almacenar las conexiones por userId
    private final Map<String, JdbcTemplate> userConnections = new ConcurrentHashMap<>();

    @Override
    public ResponseEntity<List<String>> listTables(HttpSession session) {
        JdbcTemplate jdbcTemplate = getJdbcTemplateFromSession(session);
        if (jdbcTemplate == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        try {
            // Get the list of tables in the database
            List<String> tables = schemaDiscoveryService.listTables(jdbcTemplate);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            log.error("Error listing tables", e);
            return handleException(e, "Error listing tables.");
        }
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> listColumns(String tableName, HttpSession session) {
        JdbcTemplate jdbcTemplate = getJdbcTemplateFromSession(session);
        if (jdbcTemplate == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        try {
            // Validate table name
            if (!isValidIdentifier(tableName)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Get the list of columns in the specified table
            List<Map<String, Object>> columns = schemaDiscoveryService.listColumns(tableName, jdbcTemplate);
            return ResponseEntity.ok(columns);
        } catch (Exception e) {
            log.error("Error listing columns for table: {}", tableName, e);
            return handleException(e, "Error listing columns for table.");
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getTableData(String tableName, int page, int size, HttpSession session) {
        JdbcTemplate jdbcTemplate = getJdbcTemplateFromSession(session);
        if (jdbcTemplate == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        try {
            // Validate table name
            if (!isValidIdentifier(tableName)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Retrieve table data with pagination
            ResponseEntity<DynamicTableData> responseEntity = schemaDiscoveryService.getTableDataWithPagination(tableName, jdbcTemplate, page, size);
            DynamicTableData tableData = responseEntity.getBody();

            if (tableData == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }

            // Create a response map that includes the data and pagination information
            Map<String, Object> response = new HashMap<>();
            response.put("rows", tableData.getRows());
            response.put("columns", tableData.getColumns());
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("totalRows", tableData.getTotalRows());
            response.put("tableName", tableName);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error obtaining data from table: {}", tableName, e);
            return handleException(e, "Error obtaining data from table.");
        }
    }

    // Helper method to get JdbcTemplate from session
   /* private JdbcTemplate getJdbcTemplateFromSession(HttpSession session) {
        return (JdbcTemplate) session.getAttribute(SESSION_ATTRIBUTE_CONNECTION);
    }*/
    private JdbcTemplate getJdbcTemplateFromSession(HttpSession session) {
        String userId = getUserIdFromSession(session);
        if (userId == null) {
            return null;
        }
        return userConnections.get(userId);
    }



    /**
     * Validates the provided database credentials.
     *
     * @param databaseConnectionRequest The database credentials.
     * @return True if the credentials are valid, false otherwise.
     */
    private boolean validateCredentials(DatabaseConnectionRequest databaseConnectionRequest) {
        return databaseConnectionRequest.getDatabaseName() != null && !databaseConnectionRequest.getDatabaseName().isEmpty()
                && databaseConnectionRequest.getHost() != null && !databaseConnectionRequest.getHost().isEmpty()
                && databaseConnectionRequest.getUserName() != null && !databaseConnectionRequest.getUserName().isEmpty()
                && databaseConnectionRequest.getPassword() != null && !databaseConnectionRequest.getPassword().isEmpty();
    }

    /**
     * Validates identifiers like table names to prevent SQL injection.
     *
     * @param identifier The identifier to validate.
     * @return True if the identifier is valid, false otherwise.
     */
    private boolean isValidIdentifier(String identifier) {
        return identifier != null && identifier.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * Handles exceptions based on the active environment.
     *
     * @param e       The exception that occurred.
     * @param message The message to log.
     * @return ResponseEntity with the appropriate error message.
     */
    private <T> ResponseEntity<T> handleException(Exception e, String message) {
        log.error(message, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}