package ai.leadplus.api.v1.leaddatapacks;

import ai.leadplus.application.leaddatapacks.LeadDataPackDto;
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
public class LeadDataPackResponse {

    private Long id;
    private String name;
    private String slug;
    private List<Long> industryIds;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static LeadDataPackResponse fromDto(LeadDataPackDto dto) {
        if (dto == null) return null;

        return LeadDataPackResponse.builder()
                .id(dto.getId())
                .name(dto.getName())
                .slug(dto.getSlug())
                .industryIds(dto.getIndustryIds())
                .active(dto.isActive())
                .createdBy(dto.getCreatedBy())
                .createdAt(dto.getCreatedAt())
                .updatedBy(dto.getUpdatedBy())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
