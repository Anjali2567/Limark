package ai.leadplus.application.leaddatapacks;

import ai.leadplus.domain.leaddatapacks.LeadDataPack;
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
public class LeadDataPackDto {
    private Long id;
    private String name;
    private String slug;
    private List<Long> industryIds;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    public static LeadDataPackDto fromEntity(LeadDataPack leadDataPack) {
        if (leadDataPack == null) return null;

        return LeadDataPackDto.builder()
                .id(leadDataPack.getId())
                .name(leadDataPack.getName())
                .slug(leadDataPack.getSlug())
                .industryIds(leadDataPack.getIndustryIds())
                .active(leadDataPack.isActive())
                .createdBy(leadDataPack.getCreatedBy())
                .createdAt(leadDataPack.getCreatedAt())
                .updatedBy(leadDataPack.getUpdatedBy())
                .updatedAt(leadDataPack.getUpdatedAt())
                .build();
    }

    public LeadDataPack toEntity(){
        return  LeadDataPack.builder()
                .id(id)
                .name(name)
                .slug(slug)
                .industryIds(industryIds)
                .active(active)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
