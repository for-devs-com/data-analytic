package ai.dataanalytic.databridge.service;

import ai.dataanalytic.sharedlibrary.dto.MultiDatabaseConnectionRequest;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

@Service
public class JobService {

    private final JobLauncher jobLauncher;
    private final Job databaseMigrationJob;

    public JobService(JobLauncher jobLauncher, Job databaseMigrationJob) {
        this.jobLauncher = jobLauncher;
        this.databaseMigrationJob = databaseMigrationJob;
    }

    public boolean startMigrationJob(MultiDatabaseConnectionRequest request, String tableName, String columns) {
        try {
            // Generate Job Parameters
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("sourceDatabase",
                            request.getDatabases().get(0).getDatabaseName())
                    .addString("destinationDatabase",
                            request.getDatabases().get(1).getDatabaseName())
                    .addString("tableName", tableName)
                    .addString("columns", columns)
                    .toJobParameters();

            // Start the job
            jobLauncher.run(databaseMigrationJob, jobParameters);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
