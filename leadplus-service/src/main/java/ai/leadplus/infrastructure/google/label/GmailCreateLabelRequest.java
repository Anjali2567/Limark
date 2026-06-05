package ai.leadplus.infrastructure.google.label;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GmailCreateLabelRequest {
    private String name;
    private String labelListVisibility;
    private String messageListVisibility;

    public GmailCreateLabelRequest(String name) {
        this(name, "labelShow", "show");
    }
}
