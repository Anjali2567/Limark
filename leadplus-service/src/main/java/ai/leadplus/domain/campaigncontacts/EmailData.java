package ai.leadplus.domain.campaigncontacts;

import ai.leadplus.application.campaigncontacts.EmailDeliveryStatus;
import ai.leadplus.domain.mailboxes.MailBoxType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailData {
    private int stepNumber;
    private MailBoxType emailPlatform;
    private String messageId;
    private String conversationId;
    private LocalDateTime sentAt;
    private EmailDeliveryStatus emailDeliveryStatus;
    private List<Message> messages;
}
