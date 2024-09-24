//package com.fordevs.databridge.config;
//
//import com.fordevs.databridge.entity.mysql.MySqlStudent;
//import com.fordevs.databridge.entity.postgresql.PostgreSqlStudent;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.launch.support.RunIdIncrementer;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
//import org.springframework.batch.item.database.JdbcBatchItemWriter;
//import org.springframework.batch.item.database.JdbcCursorItemReader;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.jdbc.core.BeanPropertyRowMapper;
//import org.springframework.jdbc.support.JdbcTransactionManager;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import javax.sql.DataSource;
//
//@Configuration
//public class DatabaseConfiguration extends DefaultBatchConfiguration {
//
//    @Bean
//    @Primary
//    @Qualifier("dataSource")
//    @ConfigurationProperties(prefix = "db.job.repo")
//    public DataSource dataSource() {
//        return DataSourceBuilder.create().build();  // Remove HikariDataSource specification
//    }
//
//
//    @Bean
//    @Qualifier("sourceDataSource")
//    @ConfigurationProperties(prefix = "db.source")
//    public DataSource sourceDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean
//    @Qualifier("destinationDataSource")
//    @ConfigurationProperties(prefix = "db.destination")
//    public DataSource destinationDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean
//    public PlatformTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
//        JdbcTransactionManager transactionManager = new JdbcTransactionManager();
//        transactionManager.setDataSource(dataSource);
//        return transactionManager;
//    }
//
////    @Bean
////    @Qualifier("chunkJob")
////    public Job chunkJob(
////            JobRepository jobRepository,
////            @Qualifier("firstChunkStep") Step firstChunkStep) {
////        return new JobBuilder("chunkJob", jobRepository)
////                .incrementer(new RunIdIncrementer())
////                .start(firstChunkStep)
////                .build();
////    }
//
////    @Bean
////    @Qualifier("firstChunkStep")
////    public Step firstChunkStep(
////            JobRepository jobRepository,
////            PlatformTransactionManager transactionManager,
////            @Qualifier("sourceDataSource") DataSource sourceDataSource,
////            @Qualifier("destinationDataSource") DataSource destinationDataSource) {
////        return new StepBuilder("firstChunkStep", jobRepository)
////                .<PostgreSqlStudent, MySqlStudent>chunk(200, transactionManager)
////                .reader(jdbcCursorItemReader(sourceDataSource))
////                .writer(jdbcBatchItemWriter(destinationDataSource))
////                .build();
////    }
////
////    public JdbcCursorItemReader<PostgreSqlStudent> jdbcCursorItemReader(DataSource sourceDataSource) {
////        JdbcCursorItemReader<PostgreSqlStudent> reader = new JdbcCursorItemReader<>();
////        reader.setDataSource(sourceDataSource);
////        reader.setSql("SELECT id, name, email FROM student");
////        reader.setRowMapper(new BeanPropertyRowMapper<>(PostgreSqlStudent.class));
////        return reader;
////    }
////
////    public JdbcBatchItemWriter<MySqlStudent> jdbcBatchItemWriter(DataSource destinationDataSource) {
////        JdbcBatchItemWriter<MySqlStudent> writer = new JdbcBatchItemWriter<>();
////        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
////        writer.setSql("INSERT INTO student (id, name, email) VALUES (:id, :name, :email)");
////        writer.setDataSource(destinationDataSource);
////        return writer;
////    }
//}
