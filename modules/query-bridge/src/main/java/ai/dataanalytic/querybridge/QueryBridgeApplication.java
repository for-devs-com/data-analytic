package ai.dataanalytic.querybridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "ai.dataanalytic.querybridge",
        "ai.dataanalytic.sharedlibrary"
})
public class QueryBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueryBridgeApplication.class, args);
    }

}
