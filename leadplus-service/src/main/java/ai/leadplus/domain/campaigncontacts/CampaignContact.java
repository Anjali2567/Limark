package ai.leadplus.domain.campaigncontacts;

import jakarta.persistence.Entity;
import jakarta.persistence.Convert;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import ai.leadplus.domain.common.EmailDataListConverter;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
public class CampaignContact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long campaignId;
    private Long contactId;
    private boolean participating;
    private int currentStep;
    private LocalDateTime lastSentAt;
    private LocalDateTime nextSendAt;
    private boolean replyReceived;
    @Convert(converter = EmailDataListConverter.class)
    private List<EmailData> emailData;
    @Enumerated(EnumType.STRING)
    private CampaignContactStatus status;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
