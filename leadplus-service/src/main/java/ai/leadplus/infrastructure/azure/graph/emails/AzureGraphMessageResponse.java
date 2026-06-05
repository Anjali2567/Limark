package ai.leadplus.infrastructure.azure.graph.emails;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class AzureGraphMessageResponse {
    private String id;
    private String subject;
    private Body body;
    private Sender sender;

    private List<Recipient> toRecipients;
    private List<Recipient> ccRecipients;
    private List<Recipient> bccRecipients;

    private boolean isRead;
    private String conversationId;
    private OffsetDateTime receivedDateTime;

    @Data
    public static class Body {
        private String contentType;
        private String content;
    }

    @Data
    public static class Sender {
        private EmailAddress emailAddress;

        @Data
        public static class EmailAddress {
            private String name;
            private String address;
        }
    }

    @Data
    public static class Recipient {
        private EmailAddress emailAddress;

        @Data
        public static class EmailAddress {
            private String name;
            private String address;
        }
    }
}
