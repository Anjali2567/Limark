package ai.leadplus.infrastructure.google.gmail;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.mail.internet.InternetAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GmailEmailSendRequest {

    private InternetAddress fromAddress;
    private List<InternetAddress> toAddresses;
    private List<InternetAddress> ccAddresses;
    private List<InternetAddress> bccAddresses;
    private String subject;
    private String body;
    private boolean html;

    private List<Attachment> attachments;

    @Data
    @Builder
    @AllArgsConstructor
    public static class Attachment {
        private String fileName;
        private String contentType;
        private byte[] fileData;
        private String fileUrl;
    }
}
