package ai.dataanalytic.databridge.config;

import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class QueryBridgeReader extends JdbcCursorItemReader<Map<String, Object>> {

    public QueryBridgeReader(DataSource dataSource, String query) {
        setDataSource(dataSource);
        setSql(query);
        setRowMapper((rs, rowNum) -> {
            // Dynamically map the columns to a Map
            ResultSetMetaData metaData = rs.getMetaData();
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                row.put(metaData.getColumnLabel(i), rs.getObject(i));
            }
            return row;
        });
    }
}
