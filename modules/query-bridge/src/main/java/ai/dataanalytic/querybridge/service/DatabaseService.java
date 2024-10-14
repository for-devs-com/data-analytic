package ai.dataanalytic.querybridge.service;


import ai.dataanalytic.sharedlibrary.dto.DatabaseConnectionRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Interface for database-related operations.
 */
public interface DatabaseService {

    ResponseEntity<String> setDatabaseConnection(DatabaseConnectionRequest databaseConnectionRequest);
    ResponseEntity<List<String>> listTables();
    ResponseEntity<List<Map<String, Object>>> listColumns(String tableName);
    ResponseEntity<Map<String, Object>> getTableData(String tableName, int page, int size);
    ResponseEntity<List<Map<String, Object>>> executeQuery(String query);

}
