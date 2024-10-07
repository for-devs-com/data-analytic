package ai.dataanalytic.databridge.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HealthController {

    @GetMapping("/health")
    public String healthCheck() {
        long startTime = System.currentTimeMillis();  // Inicio de la medición
        log.info("Health check endpoint was accessed");

        String response = "Data Bridge is running!";

        long endTime = System.currentTimeMillis();  // Fin de la medición
        log.debug("Health check processed in {} ms", (endTime - startTime));  // Registro del tiempo de respuesta

        return response;
    }
}
