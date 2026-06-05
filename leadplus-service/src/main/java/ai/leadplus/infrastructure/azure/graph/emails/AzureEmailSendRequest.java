package ai.leadplus.infrastructure.azure.graph.emails;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AzureEmailSendRequest {

    private String subject;
    private Body body;
    private List<Recipient> toRecipients;
    private List<Recipient> ccRecipients;
    private List<Recipient> bccRecipients;
    private List<Attachment> attachments;

    @Data
    @AllArgsConstructor
    public static class Body {
        private String contentType;
        private String content;
    }

    @Data
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Recipient {
        private AzureEmailAddressRequest emailAddress;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Attachment {
        @Builder.Default
        @JsonProperty("@odata.type")
        private String odataType = "#microsoft.graph.fileAttachment";
        private String name;
        private String contentType;
        private String contentBytes;
    }
}
