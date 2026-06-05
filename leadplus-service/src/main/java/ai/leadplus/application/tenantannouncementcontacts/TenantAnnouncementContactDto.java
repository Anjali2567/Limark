package ai.leadplus.application.tenantannouncementcontacts;

import ai.leadplus.domain.tenantannouncementcontacts.TenantAnnouncementContact;
import ai.leadplus.domain.tenantannouncementcontacts.TenantAnnouncementContactSource;
import ai.leadplus.domain.tenantannouncementcontacts.TenantAnnouncementContactStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantAnnouncementContactDto {
    private Long id;
    private Long announcementId;
    private Long sourceId;
    private TenantAnnouncementContactSource sourceType;
    private String email;
    private String firstName;
    private TenantAnnouncementContactStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    public static TenantAnnouncementContactDto fromEntity(TenantAnnouncementContact entity) {
        if (entity == null) return null;
        return TenantAnnouncementContactDto.builder()
                .id(entity.getId())
                .announcementId(entity.getAnnouncementId())
                .sourceId(entity.getSourceId())
                .sourceType(entity.getSourceType())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .status(entity.getStatus())
                .sentAt(entity.getSentAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
