package ai.dataanalytic.databridge.controller;

import ai.dataanalytic.databridge.config.DatabaseMigrationJobConfig;
import ai.dataanalytic.sharedlibrary.dto.DatabaseConnectionRequest;
import ai.dataanalytic.sharedlibrary.service.DynamicDataSourceManagerService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;

@RestController
@RequestMapping("/migration")
public class MigrationController {

    @Autowired
    private DatabaseMigrationJobConfig jobConfig;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private DynamicDataSourceManagerService dataSourceManager;

    @PostMapping("/start")
    public String migrateData(@RequestBody DatabaseConnectionRequest sourceDatabaseConnectionRequest,
                              @RequestBody DatabaseConnectionRequest destinationDatabaseConnectionRequest) {
        try {
            // Establish connection for source and destination databases
            DataSource sourceDataSource = dataSourceManager.getDataSource(sourceDatabaseConnectionRequest);
            DataSource destinationDataSource = dataSourceManager.getDataSource(destinationDatabaseConnectionRequest);

            // Initialize and start the job with the provided DataSources
            Job migrationJob = jobConfig.databaseMigrationJob(sourceDataSource, destinationDataSource);
            jobLauncher.run(migrationJob, new JobParametersBuilder()
                    .addString("time", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters());

            return "Migration job started successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to start migration job.";
        }
    }
}
