package ai.dataanalytic.sharedlibrary.dto;

import lombok.Data;

@Data
public class QueryExecutionRequest {
    private String query;
    private String databaseType;
}
