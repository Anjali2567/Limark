package ai.leadplus.application.campaigncontacts;

import ai.leadplus.domain.campaigncontacts.EmailData;
import ai.leadplus.domain.mailboxes.MailBoxType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDataDto {

    private int stepNumber;
    private MailBoxType emailPlatform;
    private String messageId;
    private String conversationId;
    private LocalDateTime sentAt;
    private EmailDeliveryStatus emailDeliveryStatus;
    private List<MessageDto> messageDtos;

    public static EmailDataDto fromEntity(EmailData emailData) {
        if (emailData == null) {
            return null;
        }
        return EmailDataDto.builder()
                .stepNumber(emailData.getStepNumber())
                .emailPlatform(emailData.getEmailPlatform())
                .messageId(emailData.getMessageId())
                .conversationId(emailData.getConversationId())
                .sentAt(emailData.getSentAt())
                .emailDeliveryStatus(emailData.getEmailDeliveryStatus())
                .messageDtos(CollectionUtils.isEmpty(emailData.getMessages()) ? null :
                        emailData.getMessages().stream()
                                .map(MessageDto::fromEntity)
                                .toList())
                .build();
    }

    public EmailData toEntity() {
        return EmailData.builder()
                .stepNumber(stepNumber)
                .emailPlatform(emailPlatform)
                .messageId(messageId)
                .conversationId(conversationId)
                .sentAt(sentAt)
                .emailDeliveryStatus(emailDeliveryStatus)
                .messages(CollectionUtils.isEmpty(messageDtos) ? null :
                        messageDtos.stream()
                                .map(MessageDto::toEntity)
                                .toList())
                .build();
    }
}
