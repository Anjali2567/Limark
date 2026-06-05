package ai.leadplus.api.v1.admin.industries;

import ai.leadplus.application.industries.IndustryDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryRequest {

    @NotBlank
    private String name;
    @NotBlank
    private String slug;
    @NotBlank
    private String description;
    private List<String> segments;

    public IndustryDto toDto() {
        return IndustryDto.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .segments(segments != null ? segments : List.of())
                .disabled(false)
                .build();
    }
}
