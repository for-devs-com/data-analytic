package ai.dataanalytic.databridge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
@Slf4j
@SpringBootApplication(scanBasePackages = "ai.dataanalytic.databridge")
public class DataBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataBridgeApplication.class, args);
        log.info("DataBridgeApplication started successfully");
    }

}
