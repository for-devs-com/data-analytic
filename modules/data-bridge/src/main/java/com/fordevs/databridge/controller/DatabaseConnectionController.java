package com.fordevs.databridge.controller;

import com.fordevs.databridge.service.QueryBridgeDatabaseClient;
import com.fordevs.sharedlibrary.dto.DatabaseConnectionRequest;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("data/bridge/database")
public class DatabaseConnectionController {

    private final QueryBridgeDatabaseClient queryBridgeDatabaseClient;

    public DatabaseConnectionController(QueryBridgeDatabaseClient queryBridgeDatabaseClient) {
        this.queryBridgeDatabaseClient = queryBridgeDatabaseClient;
    }

    @PostMapping("/connect")  // Path for database connection
    public String connectToDatabase(@RequestBody DatabaseConnectionRequest request) {
        log.info("Received request to connect to database: {}", request.getDatabaseName());
        queryBridgeDatabaseClient.connectToDatabase(request);
        return "Connection Successful!";
    }


}
