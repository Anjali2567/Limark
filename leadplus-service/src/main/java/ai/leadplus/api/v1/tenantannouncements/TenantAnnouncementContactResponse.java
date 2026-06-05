package ai.leadplus.api.v1.tenantannouncements;

import ai.leadplus.application.tenantannouncementcontacts.TenantAnnouncementContactDto;
import ai.leadplus.domain.tenantannouncementcontacts.TenantAnnouncementContactSource;
import ai.leadplus.domain.tenantannouncementcontacts.TenantAnnouncementContactStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TenantAnnouncementContactResponse {
    private Long id;
    private Long announcementId;
    private Long sourceId;
    private TenantAnnouncementContactSource sourceType;
    private String email;
    private String firstName;
    private TenantAnnouncementContactStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    public static TenantAnnouncementContactResponse fromDto(TenantAnnouncementContactDto dto) {
        return TenantAnnouncementContactResponse.builder()
                .id(dto.getId())
                .announcementId(dto.getAnnouncementId())
                .sourceId(dto.getSourceId())
                .sourceType(dto.getSourceType())
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .status(dto.getStatus())
                .sentAt(dto.getSentAt())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
