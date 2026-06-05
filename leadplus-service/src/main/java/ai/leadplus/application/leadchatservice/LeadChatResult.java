package ai.leadplus.application.leadchatservice;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeadChatResult {
    private LeadChatCompletion completion;
    private Long messageId;
}
