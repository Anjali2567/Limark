package ai.leadplus.infrastructure.hubspot.companies;

import lombok.Data;

import java.util.List;

@Data
public class HubSpotCompaniesResponse {
    private List<HubSpotCompanyResponse> results;
    private Paging paging;

    @Data
    public static class Paging {
        private Next next;

        @Data
        public static class Next {
            private String after;
        }
    }
}
