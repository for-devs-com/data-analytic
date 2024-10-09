package ai.dataanalytic.databridge.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableBatchProcessing
public class DatabaseMigrationJobConfig {

    @Bean
    public Job databaseMigrationJob(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    DataSource sourceDataSource,
                                    DataSource destinationDataSource) {
        return new JobBuilder("databaseMigrationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(migrationStep(jobRepository, transactionManager, sourceDataSource, destinationDataSource))
                .build();
    }

    @Bean
    public Step migrationStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              DataSource sourceDataSource,
                              DataSource destinationDataSource) {
        return new StepBuilder("migrationStep", jobRepository)
                .<Map<String, Object>, Map<String, Object>>chunk(100, transactionManager)
                .reader(jdbcCursorItemReader(sourceDataSource))
                .writer(jdbcBatchItemWriter(destinationDataSource))
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Map<String, Object>> jdbcCursorItemReader(DataSource sourceDataSource) {
        JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(sourceDataSource);
        reader.setSql("SELECT * FROM tableName");  // Update to dynamic tableName from UI inputs
        // Implement RowMapper here for dynamic columns.
        return reader;
    }

    @Bean
    public JdbcBatchItemWriter<Map<String, Object>> jdbcBatchItemWriter(DataSource destinationDataSource) {
        JdbcBatchItemWriter<Map<String, Object>> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(destinationDataSource);
        writer.setSql("INSERT INTO tableName (column1, column2) VALUES (:column1, :column2)");  // Update with dynamic values
        return writer;
    }
}
