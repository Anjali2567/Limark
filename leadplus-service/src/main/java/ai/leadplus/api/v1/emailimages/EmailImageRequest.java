package ai.leadplus.api.v1.emailimages;

import ai.leadplus.application.emailimages.EmailImageDto;
import ai.leadplus.domain.emailimages.EmailImageSourceType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailImageRequest {

    private Long sourceId;

    @NotNull(message = "Source type is required")
    private EmailImageSourceType sourceType;

    @NotNull(message = "File is required")
    private MultipartFile file;

    public EmailImageDto toDto() {
        return EmailImageDto.builder()
                .sourceId(sourceId)
                .sourceType(sourceType)
                .build();
    }
}
