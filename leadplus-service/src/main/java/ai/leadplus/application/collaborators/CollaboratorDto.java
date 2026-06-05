package ai.leadplus.application.collaborators;

import ai.leadplus.domain.collaborators.Collaborator;
import ai.leadplus.domain.collaborators.CollaboratorRole;
import ai.leadplus.domain.common.SourcingRequestType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CollaboratorDto {

    private Long id;
    private Long sourceId;
    private SourcingRequestType sourceType;
    private Long userId;
    private CollaboratorRole role;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private boolean active;

    public static CollaboratorDto toDto(Collaborator collaborator) {
        return baseBuilder(CollaboratorDto.builder(), collaborator)
                .build();
    }

    @SuppressWarnings("unchecked")
    protected static <T extends CollaboratorDtoBuilder<?,?>> T baseBuilder(T builder, Collaborator collaborator) {
        return (T) builder
                .id(collaborator.getId())
                .sourceId(collaborator.getSourceId())
                .sourceType(collaborator.getSourceType())
                .userId(collaborator.getUserId())
                .role(collaborator.getRole())
                .createdBy(collaborator.getCreatedBy())
                .createdAt(collaborator.getCreatedAt())
                .updatedBy(collaborator.getUpdatedBy())
                .updatedAt(collaborator.getUpdatedAt())
                .active(collaborator.isActive());
    }

    public Collaborator toEntity() {
        return Collaborator.builder()
                .id(id)
                .sourceId(sourceId)
                .sourceType(sourceType)
                .userId(userId)
                .role(role)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .active(active)
                .build();
    }
}
