package ai.dataanalytic.querybridge.config;

import ai.dataanalytic.sharedlibrary.dto.DatabaseConnectionRequest;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

@Component
public class DataSourceFactory {

    private static final Map<String, String> DRIVER_MAP = Map.of(
            "postgresql", "org.postgresql.Driver",
            "mysql", "com.mysql.cj.jdbc.Driver",
            "mariadb", "org.mariadb.jdbc.Driver",
            "sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "oracle", "oracle.jdbc.OracleDriver",
            "db2", "com.ibm.db2.jcc.DB2Driver",
            "mongodb", "mongodb.jdbc.MongoDriver"
    );

    public DataSource createDataSource(DatabaseConnectionRequest credentials) {
        HikariConfig hikariConfig = new HikariConfig();
        String driverClassName = DRIVER_MAP.get(credentials.getDatabaseType().toLowerCase());

        if (driverClassName == null) {
            throw new IllegalArgumentException("Unsupported database type: " + credentials.getDatabaseType());
        }

        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setJdbcUrl("jdbc:" +
                credentials.getDatabaseType() + "://" +
                credentials.getHost() + ":" +
                credentials.getPort() + "/" +
                credentials.getDatabaseName());
        hikariConfig.setUsername(credentials.getUserName());
        hikariConfig.setPassword(credentials.getPassword());

        return new HikariDataSource(hikariConfig);
    }
}
