package ai.dataanalytic.querybridge.service;

import ai.dataanalytic.sharedlibrary.datasource.database.DataSourceConnectionManager;
import ai.dataanalytic.querybridge.dto.DynamicTableData;
import ai.dataanalytic.sharedlibrary.dto.DatabaseConnectionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SchemaDiscoveryService {

    private final DataSourceConnectionManager dataSourceManager;

    public SchemaDiscoveryService(DataSourceConnectionManager dataSourceManager) {
        this.dataSourceManager = dataSourceManager;
    }

    public List<String> listTables(DatabaseConnectionRequest credentials) throws DataAccessException, SQLException {
        String databaseType = credentials.getDatabaseType().toLowerCase();
        String key = dataSourceManager.generateHashKey(credentials);
        log.debug("Retrieving list of tables for database type: {}, key: {}", databaseType, key);

        JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplateForDb(databaseType, key);
        if (jdbcTemplate == null) {
            log.error("JdbcTemplate not found for key: {}", key);
            throw new SQLException("Unable to obtain JdbcTemplate for given credentials.");
        }

        return jdbcTemplate.execute((Connection con) -> {
            List<String> tableList = new ArrayList<>();
            DatabaseMetaData metaData = con.getMetaData();
            log.debug("Querying tables from database metadata");
            try (ResultSet rs = metaData.getTables(null, "public", "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tableList.add(rs.getString("TABLE_NAME"));
                }
            }
            return tableList;
        });
    }

    public List<Map<String, Object>> listColumns(String tableName, String databaseType, String credentialsKey) throws SQLException {
        log.debug("Retrieving columns for table: {}, database type: {}, key: {}", tableName, databaseType, credentialsKey);

        JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplateForDb(databaseType, credentialsKey);
        if (jdbcTemplate == null) {
            log.error("JdbcTemplate not found for key: {}", credentialsKey);
            throw new SQLException("Unable to obtain JdbcTemplate for key: " + credentialsKey);
        }

        return jdbcTemplate.execute((Connection con) -> {
            List<Map<String, Object>> columnList = new ArrayList<>();
            DatabaseMetaData metaData = con.getMetaData();
            log.debug("Querying columns from database metadata for table: {}", tableName);
            try (ResultSet rs = metaData.getColumns(null, null, tableName, "%")) {
                while (rs.next()) {
                    Map<String, Object> column = new HashMap<>();
                    column.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
                    column.put("TYPE_NAME", rs.getString("TYPE_NAME"));
                    column.put("COLUMN_SIZE", rs.getInt("COLUMN_SIZE"));
                    columnList.add(column);
                }
            }
            return columnList;
        });
    }

    public ResponseEntity<DynamicTableData> getTableDataWithPagination(String tableName, DatabaseConnectionRequest credentials, int page, int size) throws SQLException {
        try {
            String databaseType = credentials.getDatabaseType().toLowerCase();
            String key = dataSourceManager.generateHashKey(credentials);
            log.debug("Retrieving table data with pagination for table: {}, page: {}, size: {}, database type: {}, key: {}", tableName, page, size, databaseType, key);

            JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplateForDb(databaseType, key);
            if (jdbcTemplate == null) {
                log.error("JdbcTemplate not found for key: {}", key);
                throw new SQLException("Unable to obtain JdbcTemplate for key: " + key);
            }

            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM " + tableName + " LIMIT ? OFFSET ?", size, page * size);
            List<Map<String, Object>> columns = listColumns(tableName, databaseType, key);
            int totalRows = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);

            DynamicTableData response = new DynamicTableData();
            response.setRows(rows);
            response.setColumns(columns);
            response.setTotalRows(totalRows);
            response.setTableName(tableName);
            response.setPageSize(size);
            response.setCurrentPage(page);

            log.info("Retrieved {} rows for table: {}, page: {}", rows.size(), tableName, page);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving table data for table: {}", tableName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
