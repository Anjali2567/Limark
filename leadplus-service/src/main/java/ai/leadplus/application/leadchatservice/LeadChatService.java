package ai.leadplus.application.leadchatservice;

import ai.leadplus.application.common.utils.FileReader;
import ai.leadplus.application.common.utils.PromptTemplateContextInjector;
import ai.leadplus.application.campaigngenerator.CampaignPromptFilterContextService;
import ai.leadplus.api.v1.leads.LeadChatRequest;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.application.messages.MessageDto;
import ai.leadplus.application.messages.MessageService;
import ai.leadplus.application.leadqueries.LeadQueryDto;
import ai.leadplus.application.leadqueries.LeadQueryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.leadplus.domain.leadqueries.LeadQueryType;
import ai.leadplus.domain.messages.MessageType;
import ai.leadplus.infrastructure.springai.SpringAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeadChatService {

    private final SpringAiClient springAiClient;
    private final LeadChatTools leadChatTools;
    private final LeadQueryService leadQueryService;
    private final CampaignPromptFilterContextService campaignPromptFilterContextService;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    private static final String TENANT_FILTER_CONTEXT_PLACEHOLDER = "{{TENANT_FILTER_CONTEXT}}";
    private static final String CURRENT_SELECTED_CRITERIA_PLACEHOLDER = "{{CURRENT_SELECTED_CRITERIA}}";
    private static final String TENANT_ID_PLACEHOLDER = "{{TENANT_ID}}";
    private static final String LEAD_QUERY_PARSING_PROMPT = FileReader.readFileContentFromClasspath("prompts/lead-chat-assistant.md");

    public LeadChatResult getLeadQueryAttributes(Long tenantId, Long userId, LeadChatRequest request) {
        Long conversationId = request.getConversationId();
        MessageDto seededMessage = null;
        if (conversationId == null) {
            seededMessage = messageService.createMessage(
                    MessageDto.builder()
                            .tenantId(tenantId)
                            .userId(userId)
                            .request(request.getRequest())
                            .build(),
                    MessageType.LEAD_CHAT_ASSISTANT
            );
            conversationId = Long.valueOf(seededMessage.getConversationId());
            request.setConversationId(conversationId);
        }

        List<LeadQueryDto> industryLeadQueries = leadQueryService.getLeadQueriesByType(LeadQueryType.COMPANY_INDUSTRY);

        List<String> industries = industryLeadQueries.stream()
                .map(LeadQueryDto::getValue)
                .toList();

        String tenantFilterContext = campaignPromptFilterContextService.buildContextBlock(tenantId);
        String promptWithContext = PromptTemplateContextInjector.injectRequired(
                LEAD_QUERY_PARSING_PROMPT,
                TENANT_FILTER_CONTEXT_PLACEHOLDER,
                tenantFilterContext,
                "LEAD_CHAT_SERVICE"
        );
        String promptWithCriteria = PromptTemplateContextInjector.injectRequired(
                promptWithContext,
                CURRENT_SELECTED_CRITERIA_PLACEHOLDER,
                stringifyCriteria(request.getCriteria()),
                "LEAD_CHAT_SERVICE"
        );
        String promptWithTenantId = PromptTemplateContextInjector.injectRequired(
                promptWithCriteria,
                TENANT_ID_PLACEHOLDER,
                String.valueOf(tenantId),
                "LEAD_CHAT_SERVICE"
        );
        String systemPrompt = promptWithTenantId.replace("{{INDUSTRY_LIST}}", String.join(", ", industries));

        LeadChatCompletion completion = springAiClient.getChatCompletion(
                request.getRequest(),
                systemPrompt,
                String.valueOf(conversationId),
                LeadChatCompletion.class,
                leadChatTools
        );

        MessageDto savedMessage;
        if (seededMessage != null) {
            savedMessage = messageService.createMessage(
                    MessageDto.builder()
                            .id(seededMessage.getId())
                            .tenantId(tenantId)
                            .userId(userId)
                            .conversationId(String.valueOf(conversationId))
                            .request(request.getRequest())
                            .response(completion.getResponse())
                            .createdAt(seededMessage.getCreatedAt())
                            .build(),
                    MessageType.LEAD_CHAT_ASSISTANT
            );
        } else {
            savedMessage = messageService.createMessage(
                    MessageDto.builder()
                            .tenantId(tenantId)
                            .userId(userId)
                            .conversationId(String.valueOf(conversationId))
                            .request(request.getRequest())
                            .response(completion.getResponse())
                            .build(),
                    MessageType.LEAD_CHAT_ASSISTANT
            );
        }

        return new LeadChatResult(completion, savedMessage.getId());
    }

    private String stringifyCriteria(LeadFilterCriteria criteria) {
        if (criteria == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(criteria);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize lead chat request criteria", e);
        }
    }
}
