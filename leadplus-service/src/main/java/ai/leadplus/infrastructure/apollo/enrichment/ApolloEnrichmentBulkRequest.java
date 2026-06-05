package ai.leadplus.infrastructure.apollo.enrichment;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ApolloEnrichmentBulkRequest {

    private List<Id> details;

    @Data
    @AllArgsConstructor
    public static class Id {
        private String id;
    }
}
