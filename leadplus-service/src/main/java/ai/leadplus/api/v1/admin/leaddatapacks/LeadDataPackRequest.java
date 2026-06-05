package ai.leadplus.api.v1.admin.leaddatapacks;

import ai.leadplus.application.leaddatapacks.LeadDataPackDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadDataPackRequest {

    @NotBlank
    private String name;
    @NotBlank
    private String slug;
    @NotEmpty
    private List<Long> industryIds;

    public LeadDataPackDto toDto() {
        return LeadDataPackDto.builder()
                .name(name)
                .slug(slug)
                .industryIds(industryIds)
                .active(true)
                .build();
    }
}