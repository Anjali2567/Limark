package ai.leadplus.domain.messages;

import jakarta.persistence.Entity;
import jakarta.persistence.Convert;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import ai.leadplus.domain.common.TargetingCriteriaConverter;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import ai.leadplus.domain.common.TargetingCriteria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long tenantId;
    private Long workspaceId;
    private String conversationId;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private MessageType type;
    private String request;
    private String response;
    private Long campaignId;
    @Convert(converter = TargetingCriteriaConverter.class)
    private TargetingCriteria targetingCriteria;
    private boolean searchPerformed;
    @CreatedDate
    private LocalDateTime createdAt;
}
