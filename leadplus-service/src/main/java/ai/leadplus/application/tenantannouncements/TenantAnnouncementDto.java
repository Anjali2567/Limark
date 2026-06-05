package ai.leadplus.application.tenantannouncements;

import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.common.utils.RecipientUtils;
import ai.leadplus.domain.tenantannouncements.TenantAnnouncement;
import ai.leadplus.domain.tenantannouncements.TenantAnnouncementStatus;
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
public class TenantAnnouncementDto {
    private Long id;
    private Long tenantId;
    private String name;
    private List<RecipientDto> ccRecipients;
    private List<RecipientDto> bccRecipients;
    private String subject;
    private String body;
    private List<String> attachmentIds;
    private boolean active;
    private TenantAnnouncementStatus status;
    private long recipientCount;
    private LocalDateTime launchedAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static TenantAnnouncementDto fromEntity(TenantAnnouncement tenantAnnouncement) {
        if (tenantAnnouncement == null) return null;
        return TenantAnnouncementDto.builder()
                .id(tenantAnnouncement.getId())
                .tenantId(tenantAnnouncement.getTenantId())
                .name(tenantAnnouncement.getName())
                .ccRecipients(RecipientUtils.mapToDto(tenantAnnouncement.getCcRecipients()))
                .bccRecipients(RecipientUtils.mapToDto(tenantAnnouncement.getBccRecipients()))
                .subject(tenantAnnouncement.getSubject())
                .body(tenantAnnouncement.getBody())
                .attachmentIds(tenantAnnouncement.getAttachmentIds())
                .status(tenantAnnouncement.getStatus())
                .launchedAt(tenantAnnouncement.getLaunchedAt())
                .createdBy(tenantAnnouncement.getCreatedBy())
                .createdAt(tenantAnnouncement.getCreatedAt())
                .updatedBy(tenantAnnouncement.getUpdatedBy())
                .updatedAt(tenantAnnouncement.getUpdatedAt())
                .active(tenantAnnouncement.isActive())
                .build();
    }

    public TenantAnnouncement toEntity() {
        return TenantAnnouncement.builder()
                .id(id)
                .tenantId(tenantId)
                .name(name)
                .ccRecipients(RecipientUtils.mapToEntity(ccRecipients))
                .bccRecipients(RecipientUtils.mapToEntity(bccRecipients))
                .subject(subject)
                .body(body)
                .attachmentIds(attachmentIds)
                .status(status)
                .launchedAt(launchedAt)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .active(active)
                .build();
    }
}
