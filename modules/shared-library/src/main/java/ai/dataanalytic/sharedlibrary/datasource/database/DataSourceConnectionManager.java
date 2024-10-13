package ai.dataanalytic.sharedlibrary.datasource.database;


import ai.dataanalytic.sharedlibrary.dto.DatabaseConnectionRequest;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DataSourceConnectionManager {

    private final Map<String, JdbcTemplate> dataSourceCache = new ConcurrentHashMap<>();
    private final DataSourceFactory dataSourceFactory;
    private final ConnectionTester connectionTester;

    @Autowired
    public DataSourceConnectionManager(DataSourceFactory dataSourceFactory, ConnectionTester connectionTester) {
        this.dataSourceFactory = dataSourceFactory;
        this.connectionTester = connectionTester;
    }

    public boolean createAndTestConnection(DatabaseConnectionRequest credentials) {
        String key = generateKey(credentials);
        log.debug("Creating and testing connection for key: {}", key);

        JdbcTemplate jdbcTemplate = dataSourceCache.computeIfAbsent(key, k -> {
            DataSource dataSource = dataSourceFactory.createDataSource(credentials);
            log.debug("DataSource created for key: {}", key);
            return new JdbcTemplate(dataSource);
        });

        if (connectionTester.testConnection(jdbcTemplate)) {
            DataSourceContextService.setCurrentTemplate(jdbcTemplate);
            log.debug("Connection successful for key: {}", key);
            return true;
        } else {
            closeDataSource(key);
            log.debug("Connection failed for key: {}", key);
            return false;
        }
    }

    public JdbcTemplate getJdbcTemplateForDb(String key) {
        log.debug("Retrieving JdbcTemplate for key: {}", key);
        return dataSourceCache.getOrDefault(key, DataSourceContextService.getCurrentTemplate());
    }

    public void closeDataSource(String key) {
        log.debug("Closing DataSource for key: {}", key);
        JdbcTemplate jdbcTemplate = dataSourceCache.remove(key);
        DataSourceContextService.clear();

        if (jdbcTemplate != null) {
            DataSource dataSource = jdbcTemplate.getDataSource();
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
                log.debug("HikariDataSource closed for key: {}", key);
            }
        }
    }

    public String getKey(DatabaseConnectionRequest credentials) {
        String key = generateKey(credentials);
        log.debug("Generated key: {}", key);
        return key;
    }

    private String generateKey(DatabaseConnectionRequest credentials) {
        return credentials.getDatabaseType() + "-" +
                credentials.getHost() + "-" +
                credentials.getPort() + "-" +
                credentials.getDatabaseName() + "-" +
                credentials.getUserName() + "-" +
                credentials.getPassword();
    }
}
