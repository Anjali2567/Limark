package ai.leadplus.domain.vendors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialMedia {
    private String linkedin;
    private String twitter;
    private String facebook;
    private String instagram;
    private String youtube;
}
