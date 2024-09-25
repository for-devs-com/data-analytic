package ai.dataanalytic.databridge.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DatabaseConfiguration extends DefaultBatchConfiguration {

    @Bean
    @Primary
    @Qualifier("dataSource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Qualifier("sourceDataSource")
    public DataSource sourceDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Qualifier("destinationDataSource")
    public DataSource destinationDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
        JdbcTransactionManager transactionManager = new JdbcTransactionManager();
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }

    @Bean
    @Qualifier("chunkJob")
    public Job chunkJob(
            JobRepository jobRepository,
            @Qualifier("firstChunkStep") Step firstChunkStep) {
        return new JobBuilder("chunkJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(firstChunkStep)
                .build();
    }

    @Bean
    @Qualifier("firstChunkStep")
    public Step firstChunkStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("queryBridgeReader") ItemReader<Map<String, Object>> queryBridgeReader,
            @Qualifier("queryBridgeWriter") ItemWriter<Map<String, Object>> queryBridgeWriter) {
        return new StepBuilder("firstChunkStep", jobRepository)
                .<Map<String, Object>, Map<String, Object>>chunk(200, transactionManager)
                .reader(queryBridgeReader)
                .writer(queryBridgeWriter)
                .build();
    }

    // The ItemReader uses Query-Bridge API to dynamically fetch data
    @Bean
    @Qualifier("queryBridgeReader")
    public ItemReader<Map<String, Object>> queryBridgeReader() {
        return new QueryBridgeReader();
    }

    // The ItemWriter uses Query-Bridge API to write the data back
    @Bean
    @Qualifier("queryBridgeWriter")
    public ItemWriter<Map<String, Object>> queryBridgeWriter() {
        return new QueryBridgeWriter();
    }
}
