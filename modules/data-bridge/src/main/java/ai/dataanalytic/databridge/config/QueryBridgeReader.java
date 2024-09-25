package ai.dataanalytic.databridge.config;

import ai.dataanalytic.databridge.service.QueryBridgeClient;
import org.springframework.batch.item.ItemReader;
import java.util.Map;

public class QueryBridgeReader implements ItemReader<Map<String, Object>> {

    private final QueryBridgeClient queryBridgeClient;

    public QueryBridgeReader(QueryBridgeClient queryBridgeClient) {
        this.queryBridgeClient = queryBridgeClient;
    }

    @Override
    public Map<String, Object> read() throws Exception {
        return queryBridgeClient.fetchNextRow(); // Calling QueryBridge to fetch row data
    }
}
