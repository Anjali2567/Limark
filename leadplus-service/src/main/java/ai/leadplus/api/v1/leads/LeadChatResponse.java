package ai.leadplus.api.v1.leads;

import ai.leadplus.application.leadchatservice.LeadChatCompletion;
import ai.leadplus.application.leads.LeadFilterCriteria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadChatResponse {

    private Long messageId;
    private Long conversationId;
    private String request;
    private String response;
    private LeadFilterCriteria criteria;

    public static LeadChatResponse fromCompletion(LeadChatCompletion completion, String request) {
        return LeadChatResponse.builder()
                .request(request)
                .response(completion.getResponse())
                .criteria(completion.getCriteria())
                .build();
    }
}
