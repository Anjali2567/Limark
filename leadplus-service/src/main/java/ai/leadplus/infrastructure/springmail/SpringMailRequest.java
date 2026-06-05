package ai.leadplus.infrastructure.springmail;

import jakarta.mail.internet.InternetAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpringMailRequest {
    private String subject;
    private String body;
    private InternetAddress fromAddress;
    private InternetAddress[] toAddresses;
    private InternetAddress[] ccAddresses;
    private InternetAddress[] bccAddresses;
    private List<Attachment> attachments;

    @Data
    @Builder
    public static class Attachment {
        private String fileName;
        private ByteArrayResource resource;
        private String contentType;
    }
}
