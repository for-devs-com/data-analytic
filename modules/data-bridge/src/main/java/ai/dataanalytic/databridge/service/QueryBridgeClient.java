package ai.dataanalytic.databridge.service;


import ai.dataanalytic.sharedlibrary.dto.DatabaseConnectionRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class QueryBridgeClient {
    private final RestTemplate restTemplate;
    private final String queryBridgeUrl = "http://localhost:8081/query/bridge/database";

    public QueryBridgeClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void connectToDatabase(DatabaseConnectionRequest request) {
        restTemplate.postForEntity(queryBridgeUrl + "/connect", request, Void.class);
    }

    public Object fetchAllData(String query) {
        return restTemplate.getForObject(queryBridgeUrl + "/query?sql=" + query, Object.class);
    }

    public void writeRow(Map<String, Object> row) {
    }
}
