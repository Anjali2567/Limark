package ai.leadplus.application.campaignagent.tools;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class IndustryAccessDto {
    private String industry;
    private List<String> accessibleSegments;
}
