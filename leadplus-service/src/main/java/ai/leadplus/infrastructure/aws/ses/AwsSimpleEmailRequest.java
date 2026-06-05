package ai.leadplus.infrastructure.aws.ses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AwsSimpleEmailRequest {
    private String senderName;
    private String senderEmail;
    private List<String> toRecipients;
    private List<String> ccRecipients;
    private List<String> bccRecipients;
    private String replyToEmail;
    private String subject;
    private String body;
    private List<AwsSimpleEmailAttachment> attachments;

    @Data
    @Builder
    public static class AwsSimpleEmailAttachment {
        private String fileName;
        private byte[] fileBytes;
        private String contentType;
    }
}
