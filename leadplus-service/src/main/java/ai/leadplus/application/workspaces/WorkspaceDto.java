package ai.leadplus.application.workspaces;

import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.application.common.utils.RecipientUtils;
import ai.leadplus.domain.workspaces.Workspace;
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
public class WorkspaceDto {

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

    public static WorkspaceDto fromEntity(Workspace workspace) {
        if (workspace == null) {
            return null;
        }

        return WorkspaceDto.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .ownerId(workspace.getOwnerId())
                .tenantId(workspace.getTenantId())
                .ccRecipients(RecipientUtils.mapToDto(workspace.getCcRecipients()))
                .bccRecipients(RecipientUtils.mapToDto(workspace.getBccRecipients()))
                .dailySendLimit(workspace.getDailySendLimit())
                .createdBy(workspace.getCreatedBy())
                .createdAt(workspace.getCreatedAt())
                .updatedBy(workspace.getUpdatedBy())
                .updatedAt(workspace.getUpdatedAt())
                .build();
    }

    public Workspace toEntity() {
        return Workspace.builder()
                .id(id)
                .name(name)
                .ownerId(ownerId)
                .tenantId(tenantId)
                .ccRecipients(RecipientUtils.mapToEntity(ccRecipients))
                .bccRecipients(RecipientUtils.mapToEntity(bccRecipients))
                .dailySendLimit(dailySendLimit)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
