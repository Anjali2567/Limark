package ai.leadplus.application.industries;

import ai.leadplus.domain.industries.Industry;
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
public class IndustryDto {

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

    public static IndustryDto fromEntity(Industry industry) {
        return IndustryDto.builder()
                .id(industry.getId())
                .name(industry.getName())
                .slug(industry.getSlug())
                .description(industry.getDescription())
                .image(industry.getImage())
                .segments(industry.getSegments())
                .disabled(industry.isDisabled())
                .active(industry.isActive())
                .createdBy(industry.getCreatedBy())
                .createdAt(industry.getCreatedAt())
                .updatedBy(industry.getUpdatedBy())
                .updatedAt(industry.getUpdatedAt())
                .build();
    }

    public Industry toEntity() {
        return Industry.builder()
                .id(id)
                .name(name)
                .slug(slug)
                .description(description)
                .image(image)
                .segments(segments)
                .disabled(disabled)
                .active(active)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
