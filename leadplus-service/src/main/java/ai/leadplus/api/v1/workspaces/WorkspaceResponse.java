package ai.leadplus.api.v1.workspaces;

import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.workspaces.WorkspaceDto;
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
public class WorkspaceResponse {

    private Long id;
    private String name;
    private Long ownerId;
    private Long tenantId;
    private List<RecipientDto> ccRecipients;
    private List<RecipientDto> bccRecipients;
    private int dailySendLimit;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static WorkspaceResponse fromDto(WorkspaceDto workspaceDto) {
        return WorkspaceResponse.builder()
                .id(workspaceDto.getId())
                .name(workspaceDto.getName())
                .ownerId(workspaceDto.getOwnerId())
                .tenantId(workspaceDto.getTenantId())
                .ccRecipients(workspaceDto.getCcRecipients())
                .bccRecipients(workspaceDto.getBccRecipients())
                .dailySendLimit(workspaceDto.getDailySendLimit())
                .createdBy(workspaceDto.getCreatedBy())
                .createdAt(workspaceDto.getCreatedAt())
                .updatedBy(workspaceDto.getUpdatedBy())
                .updatedAt(workspaceDto.getUpdatedAt())
                .build();
    }
}
