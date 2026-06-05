package ai.leadplus.application.campaigncontacts;

import ai.leadplus.domain.campaigncontacts.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private String fromAddress;
    private List<String> toAddresses;
    private List<String> ccAddresses;
    private List<String> bccAddresses;
    private String subject;
    private String body;

    public static MessageDto fromEntity(Message message) {
        return MessageDto.builder()
                .fromAddress(message.getFromAddress())
                .toAddresses(message.getToAddresses())
                .ccAddresses(message.getCcAddresses())
                .bccAddresses(message.getBccAddresses())
                .subject(message.getSubject())
                .body(message.getBody())
                .build();
    }

    public Message toEntity() {
        return Message.builder()
                .fromAddress(fromAddress)
                .toAddresses(toAddresses)
                .ccAddresses(ccAddresses)
                .bccAddresses(bccAddresses)
                .subject(subject)
                .body(body)
                .build();
    }
}