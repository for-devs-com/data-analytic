package ai.dataanalytic.querybridge.service;


import ai.dataanalytic.sharedlibrary.dto.DatabaseConnectionRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Interface for database-related operations.
 */
public interface DatabaseService {

    /**
     * Connects to the database using dynamic data sources.
     *
     * @param databaseConnectionRequest The database credentials provided in the request body.
     * @param session                   The HTTP session.
     * @return ResponseEntity with connection status.
     */
    ResponseEntity<String> setDatabaseConnection(DatabaseConnectionRequest databaseConnectionRequest, HttpSession session);

    /**
     * Lists the tables in the database.
     *
     * @param session The HTTP session.
     * @return ResponseEntity with the list of tables.
     */
    ResponseEntity<List<String>> listTables(HttpSession session);

    /**
     * Lists the columns of a table.
     *
     * @param tableName The name of the table.
     * @param session   The HTTP session.
     * @return ResponseEntity with the list of columns.
     */
    ResponseEntity<List<Map<String, Object>>> listColumns(String tableName, HttpSession session);

    /**
     * Gets the data of a table with pagination.
     *
     * @param tableName The name of the table.
     * @param page      The page number.
     * @param size      The number of rows per page.
     * @param session   The HTTP session.
     * @return ResponseEntity with the table data.
     */
    ResponseEntity<Map<String, Object>> getTableData(String tableName, int page, int size, HttpSession session);
}