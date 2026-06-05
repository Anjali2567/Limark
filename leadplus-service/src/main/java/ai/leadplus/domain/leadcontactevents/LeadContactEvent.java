package ai.leadplus.domain.leadcontactevents;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
public class LeadContactEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long tenantId;
    private Long workspaceId;
    private Long contactId;
    @Enumerated(EnumType.STRING)
    private LeadContactEventCategory category;
    @Enumerated(EnumType.STRING)
    private LeadContactEventType type;
    private String description;
    private Long sourceId;
    @Enumerated(EnumType.STRING)
    private LeadContactEventSourceType sourceType;
    private String eventBy;
    private LocalDateTime eventAt;
    private boolean active;
    @CreatedDate
    private LocalDateTime createdAt;
}
