package ai.leadplus.api.v1.tenantannouncements;

import ai.leadplus.application.tenantannouncements.TenantAnnouncementDto;
import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.domain.tenantannouncements.TenantAnnouncementStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TenantAnnouncementResponse {
    private Long id;
    private Long tenantId;
    private String name;
    private String subject;
    private String body;
    private List<RecipientDto> ccRecipients;
    private List<RecipientDto> bccRecipients;
    private List<String> attachmentIds;
    private TenantAnnouncementStatus status;
    private long recipientCount;
    private LocalDateTime launchedAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static TenantAnnouncementResponse fromDto(TenantAnnouncementDto dto) {
        return TenantAnnouncementResponse.builder()
                .id(dto.getId())
                .tenantId(dto.getTenantId())
                .name(dto.getName())
                .subject(dto.getSubject())
                .body(dto.getBody())
                .ccRecipients(dto.getCcRecipients())
                .bccRecipients(dto.getBccRecipients())
                .attachmentIds(dto.getAttachmentIds())
                .status(dto.getStatus())
                .recipientCount(dto.getRecipientCount())
                .launchedAt(dto.getLaunchedAt())
                .createdBy(dto.getCreatedBy())
                .createdAt(dto.getCreatedAt())
                .updatedBy(dto.getUpdatedBy())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
