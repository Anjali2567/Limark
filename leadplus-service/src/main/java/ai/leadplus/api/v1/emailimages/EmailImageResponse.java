package ai.leadplus.api.v1.emailimages;

import ai.leadplus.application.emailimages.EmailImageDto;
import ai.leadplus.domain.emailimages.EmailImageSourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailImageResponse {

    private Long id;
    private Long sourceId;
    private EmailImageSourceType sourceType;
    private String resourceUrl;
    private Long createdBy;
    private LocalDateTime createdAt;

    public static EmailImageResponse toResponse(EmailImageDto dto) {
        return EmailImageResponse.builder()
                .id(dto.getId())
                .sourceId(dto.getSourceId())
                .sourceType(dto.getSourceType())
                .resourceUrl(dto.getResourceUrl())
                .createdBy(dto.getCreatedBy())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
