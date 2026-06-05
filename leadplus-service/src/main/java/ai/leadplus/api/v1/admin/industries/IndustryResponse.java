package ai.leadplus.api.v1.admin.industries;

import ai.leadplus.application.industries.IndustryDto;
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
public class IndustryResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String image;
    private List<String> segments;
    private boolean disabled;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static IndustryResponse fromDto(IndustryDto industryDto) {
        return IndustryResponse.builder()
                .id(industryDto.getId())
                .name(industryDto.getName())
                .slug(industryDto.getSlug())
                .description(industryDto.getDescription())
                .image(industryDto.getImage())
                .segments(industryDto.getSegments())
                .disabled(industryDto.isDisabled())
                .active(industryDto.isActive())
                .createdBy(industryDto.getCreatedBy())
                .createdAt(industryDto.getCreatedAt())
                .updatedBy(industryDto.getUpdatedBy())
                .updatedAt(industryDto.getUpdatedAt())
                .build();
    }
}
