package ai.dataanalytic.databridge.service;


import ai.dataanalytic.sharedlibrary.dto.DatabaseConnectionRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class QueryBridgeClient {
    private final RestTemplate restTemplate;

    public QueryBridgeClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

}
