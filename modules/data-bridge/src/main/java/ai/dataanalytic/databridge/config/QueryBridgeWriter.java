package ai.dataanalytic.databridge.config;

import org.springframework.batch.item.database.ItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;
import java.util.Map;

public class QueryBridgeWriter extends JdbcBatchItemWriter<Map<String, Object>> {

    public QueryBridgeWriter(DataSource dataSource, String insertSql) {
        setDataSource(dataSource);
        setSql(insertSql);
        setItemSqlParameterSourceProvider(new MapSqlParameterSourceProvider());
    }

    private static class MapSqlParameterSourceProvider implements ItemSqlParameterSourceProvider<Map<String, Object>> {
        @Override
        public SqlParameterSource createSqlParameterSource(Map<String, Object> item) {
            return new MapSqlParameterSource(item);
        }
    }
}
