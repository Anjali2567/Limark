package ai.leadplus.infrastructure.google.label;

import lombok.Data;

@Data
public class GmailLabelResponse {
    private String id;
    private String name;
    private String messageListVisibility;
    private String labelListVisibility;
}
