package ai.leadplus.application.leadchatservice;

import ai.leadplus.application.leads.LeadFilterCriteria;
import lombok.Data;

@Data
public class LeadChatCompletion {
    private String response;
    private LeadFilterCriteria criteria;
}
