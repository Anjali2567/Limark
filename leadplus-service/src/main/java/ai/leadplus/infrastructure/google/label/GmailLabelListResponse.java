package ai.leadplus.infrastructure.google.label;

import lombok.Data;

import java.util.List;

@Data
public class GmailLabelListResponse {
    private List<GmailLabelResponse> labels;
}
