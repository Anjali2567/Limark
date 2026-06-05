package ai.leadplus.application.emailimages;

import ai.leadplus.domain.emailimages.EmailImage;
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
public class EmailImageDto {

    private Long id;
    private Long sourceId;
    private EmailImageSourceType sourceType;
    private String resourceUrl;
    private Long createdBy;
    private LocalDateTime createdAt;

    public static EmailImageDto fromEntity(EmailImage emailImage) {
        return EmailImageDto.builder()
                .id(emailImage.getId())
                .sourceId(emailImage.getSourceId())
                .sourceType(emailImage.getSourceType())
                .resourceUrl(emailImage.getResourceUrl())
                .createdBy(emailImage.getCreatedBy())
                .createdAt(emailImage.getCreatedAt())
                .build();
    }

    public EmailImage toEntity() {
        return EmailImage.builder()
                .id(id)
                .sourceId(sourceId)
                .sourceType(sourceType)
                .resourceUrl(resourceUrl)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .build();
    }
}
