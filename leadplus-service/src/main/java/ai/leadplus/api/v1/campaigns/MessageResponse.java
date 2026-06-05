package ai.leadplus.api.v1.campaigns;

import ai.leadplus.application.campaigncontacts.MessageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String fromAddress;
    private List<String> toAddresses;
    private List<String> ccAddresses;
    private List<String> bccAddresses;
    private String subject;
    private String body;

    public static MessageResponse fromDto(MessageDto message) {
        return MessageResponse.builder()
                .fromAddress(message.getFromAddress())
                .toAddresses(message.getToAddresses())
                .ccAddresses(message.getCcAddresses())
                .bccAddresses(message.getBccAddresses())
                .subject(message.getSubject())
                .body(message.getBody())
                .build();
    }
}
