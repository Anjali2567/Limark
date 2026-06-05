package ai.leadplus.api.v1.common.collaborators;

import ai.leadplus.application.collaborators.CollaboratorDto;
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
public class CollaboratorResponse {

    private Long id;
    private Long sourceId;
    private SourcingRequestType sourceType;
    private Long userId;
    private CollaboratorRole role;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static CollaboratorResponse toResponse(CollaboratorDto dto) {
        return baseBuilder(CollaboratorResponse.builder(), dto)
                .build();
    }

    @SuppressWarnings("unchecked")
    protected static <T extends CollaboratorResponseBuilder<?,?>> T baseBuilder(T builder, CollaboratorDto dto) {
        return (T) builder
                .id(dto.getId())
                .sourceId(dto.getSourceId())
                .sourceType(dto.getSourceType())
                .userId(dto.getUserId())
                .role(dto.getRole())
                .createdBy(dto.getCreatedBy())
                .createdAt(dto.getCreatedAt())
                .updatedBy(dto.getUpdatedBy())
                .updatedAt(dto.getUpdatedAt());
    }
}
