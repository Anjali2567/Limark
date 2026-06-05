package ai.leadplus.infrastructure.hubspot.contacts;

import lombok.Data;
import java.util.List;

@Data
public class HubSpotContactsResponse {
    private List<HubSpotContactResponse> results;
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