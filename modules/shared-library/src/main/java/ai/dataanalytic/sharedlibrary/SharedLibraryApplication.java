package ai.dataanalytic.sharedlibrary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "ai.dataanalytic.sharedlibrary"
})
public class SharedLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(SharedLibraryApplication.class, args);
    }
}
