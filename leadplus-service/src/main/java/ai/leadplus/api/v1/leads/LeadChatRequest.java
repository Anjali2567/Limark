package ai.leadplus.api.v1.leads;

import ai.leadplus.application.leads.LeadFilterCriteria;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeadChatRequest {
    @NotBlank(message = "Request is mandatory")
    private String request;
    private Long conversationId;
    private LeadFilterCriteria criteria;
}
