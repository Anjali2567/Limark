package ai.leadplus.application.leadchatservice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadResultPreview {
    private String scope;
    private boolean hasResults;
    private long totalResults;
    private List<String> samples;
    private String feedback;
}
