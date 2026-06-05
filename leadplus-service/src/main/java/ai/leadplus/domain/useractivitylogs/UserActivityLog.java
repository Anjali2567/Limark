package ai.leadplus.domain.useractivitylogs;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long tenantId;
    private Long workspaceId;
    private Long userId;
    private String username;
    private String message;
    @Enumerated(EnumType.STRING)
    private LogLevel logLevel;
    @Enumerated(EnumType.STRING)
    private UserActivityLogType type;
    @CreatedDate
    private LocalDateTime createdAt;
    @CreatedBy
    private Long createdBy;
}
