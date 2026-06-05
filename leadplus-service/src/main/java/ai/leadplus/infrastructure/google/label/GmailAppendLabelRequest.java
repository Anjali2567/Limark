package ai.leadplus.infrastructure.google.label;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GmailAppendLabelRequest {
    private List<String> addLabelIds;
}
