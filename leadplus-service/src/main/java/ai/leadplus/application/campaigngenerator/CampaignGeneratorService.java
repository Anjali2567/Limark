package ai.leadplus.application.campaigngenerator;

import ai.leadplus.application.campaigngenerator.tools.CampaignTools;
import ai.leadplus.application.campaigngenerator.tools.LeadGeneratorSearchTools;
import ai.leadplus.application.campaigns.CampaignDto;
import ai.leadplus.application.campaigns.CampaignService;
import ai.leadplus.application.chat.SpringAiChatMemory;
import ai.leadplus.application.common.LocationFilterDto;
import ai.leadplus.application.common.utils.FileReader;
import ai.leadplus.application.common.utils.PromptTemplateContextInjector;
import ai.leadplus.application.common.TargetingCriteriaDto;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.messages.MessageDto;
import ai.leadplus.application.messages.MessageService;
import ai.leadplus.domain.messages.MessageType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CampaignGeneratorService {
    private static final String TENANT_FILTER_CONTEXT_PLACEHOLDER = "{{TENANT_FILTER_CONTEXT}}";
    private static final String CAMPAIGN_GENERATOR_PROMPT =
            FileReader.readFileContentFromClasspath("prompts/campaign-generator.md");

    private final ChatClient chatClient;
    private final LeadGeneratorSearchTools leadGeneratorSearchTools;
    private final CampaignTools campaignTools;
    private final MessageService messageService;
    private final CampaignService campaignService;
    private final CampaignPromptFilterContextService campaignPromptFilterContextService;

    public CampaignGeneratorService(SpringAiChatMemory chatMessageMemory,
                                    OpenAiChatModel chatModel,
                                    LeadGeneratorSearchTools leadGeneratorSearchTools,
                                    CampaignTools campaignTools,
                                    MessageService messageService,
                                    CampaignService campaignService,
                                    CampaignPromptFilterContextService campaignPromptFilterContextService) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMessageMemory)
                .build();

        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.leadGeneratorSearchTools = leadGeneratorSearchTools;
        this.campaignTools = campaignTools;
        this.messageService = messageService;
        this.campaignService = campaignService;
        this.campaignPromptFilterContextService = campaignPromptFilterContextService;
    }

    public CampaignGeneratorCompletion getChatCompletion(Long tenantId, Long workspaceId, MessageDto messageDto) {
        boolean isNewConversation = !StringUtils.hasText(messageDto.getConversationId());
        CampaignDto campaignDto = isNewConversation
                ? campaignService.createBasicCampaign(tenantId, workspaceId)
                : campaignService.findCampaignByConversationId(workspaceId, messageDto.getConversationId());
        String conversationId = isNewConversation
                ? "-"
                : messageDto.getConversationId();

        String systemPromptWithSessionId = CAMPAIGN_GENERATOR_PROMPT.replace("{{CAMPAIGN_ID}}", String.valueOf(campaignDto.getId()));
        String filterContext = campaignPromptFilterContextService.buildContextBlock(tenantId);
        String systemPrompt = PromptTemplateContextInjector.injectRequired(
                systemPromptWithSessionId,
                TENANT_FILTER_CONTEXT_PLACEHOLDER,
                filterContext,
                "CAMPAIGN_GENERATOR"
        );

        CampaignGeneratorCompletion campaignGeneratorCompletion = chatClient
                .prompt()
                .tools(leadGeneratorSearchTools, campaignTools)
                .advisors(advisor -> advisor.param(org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID, conversationId))
                .user(messageDto.getRequest())
                .system(systemPrompt)
                .call()
                .entity(CampaignGeneratorCompletion.class);
        if (campaignGeneratorCompletion.getCampaignId() == null) {
            campaignGeneratorCompletion.setCampaignId(campaignDto.getId());
        }
        return campaignGeneratorCompletion;
    }

    public MessageDto getMessageReply(Long tenantId, Long workspaceId, Long userId, MessageDto messageDto) {
        CampaignGeneratorCompletion chatCompletion = getChatCompletion(tenantId, workspaceId, messageDto);
        Long campaignId = chatCompletion.getCampaignId();
        if (chatCompletion.isInsufficientContext()) {
            campaignService.deleteCampaignById(campaignId);
            throw new BadRequestException(chatCompletion.getResponse());
        }

        CampaignDto campaignDto = campaignService.getCampaignById(campaignId);
        TargetingCriteriaDto targetingCriteriaDto = campaignDto.getTargetingCriteria();
        messageDto.setTenantId(tenantId);
        messageDto.setWorkspaceId(workspaceId);
        messageDto.setUserId(userId);
        messageDto.setCampaignState(targetingCriteriaDto);
        messageDto.setResponse(chatCompletion.getResponse());
        messageDto.setCampaignId(campaignId);

        if (CollectionUtils.isEmpty(targetingCriteriaDto.getCompanyIds()) || CollectionUtils.isEmpty(targetingCriteriaDto.getContactIds())) {
            if (!StringUtils.hasText(messageDto.getConversationId())) campaignService.deleteCampaignById(campaignId);
            throw new BadRequestException(buildNoEligibleLeadsMessage(
                    campaignDto,
                    targetingCriteriaDto,
                    chatCompletion
            ));
        }

        MessageDto savedMessage = messageService.createMessage(messageDto, MessageType.CAMPAIGN_GENERATOR);
        targetingCriteriaDto.setConversationId(savedMessage.getConversationId());
        campaignDto.setTargetingCriteria(targetingCriteriaDto);
        campaignDto.setName(chatCompletion.getCampaignName());
        campaignDto.setIndustry(chatCompletion.getCampaignIndustry());
        campaignService.updateCampaign(campaignDto);
        return savedMessage;
    }

    private String buildNoEligibleLeadsMessage(CampaignDto campaignDto,
                                               TargetingCriteriaDto targetingCriteriaDto,
                                               CampaignGeneratorCompletion chatCompletion) {
        if (targetingCriteriaDto == null) {
            return "No eligible leads found for your current criteria. Try broadening location, company size, or job-title filters.";
        }

        boolean noCompanies = CollectionUtils.isEmpty(targetingCriteriaDto.getCompanyIds());
        String outcomeDetail = noCompanies
                ? "No companies matched the current company-level filters."
                : "Companies matched, but no campaign-eligible contacts matched the contact or outreach criteria.";

        List<String> criteriaParts = new ArrayList<>();
        String industry = StringUtils.hasText(campaignDto.getIndustry())
                ? campaignDto.getIndustry()
                : (chatCompletion != null ? chatCompletion.getCampaignIndustry() : null);
        if (StringUtils.hasText(industry)) {
            criteriaParts.add("industry: %s".formatted(industry));
        }
        if (StringUtils.hasText(targetingCriteriaDto.getSearchQuery())) {
            criteriaParts.add("keywords: %s".formatted(targetingCriteriaDto.getSearchQuery()));
        }
        if (!CollectionUtils.isEmpty(targetingCriteriaDto.getEmployeeRanges())) {
            criteriaParts.add("employee ranges: %s".formatted(targetingCriteriaDto.getEmployeeRanges()));
        }
        if (!CollectionUtils.isEmpty(targetingCriteriaDto.getJobTitles())) {
            criteriaParts.add("job titles: %s".formatted(String.join(", ", targetingCriteriaDto.getJobTitles())));
        }
        String companyLocation = formatLocationFilter(targetingCriteriaDto.getCompanyLocationFilter());
        if (StringUtils.hasText(companyLocation)) {
            criteriaParts.add("company location: %s".formatted(companyLocation));
        }
        String contactLocation = formatLocationFilter(targetingCriteriaDto.getContactLocationFilter());
        if (StringUtils.hasText(contactLocation)) {
            criteriaParts.add("contact location: %s".formatted(contactLocation));
        }

        String appliedCriteria = CollectionUtils.isEmpty(criteriaParts)
                ? "no explicit filter values were parsed into structured criteria"
                : String.join("; ", criteriaParts);

        return """
                No eligible leads found for your current criteria.
                %s
                We searched with: %s.
                Results are limited to your tenant-accessible data context.
                Try broadening one filter first: remove location constraints, widen employee ranges, or use broader job titles.
                """.formatted(outcomeDetail, appliedCriteria);
    }

    private String formatLocationFilter(LocationFilterDto locationFilterDto) {
        if (locationFilterDto == null) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        if (!CollectionUtils.isEmpty(locationFilterDto.getCities())) {
            parts.add("cities=%s".formatted(String.join(", ", locationFilterDto.getCities())));
        }
        if (!CollectionUtils.isEmpty(locationFilterDto.getStates())) {
            parts.add("states=%s".formatted(String.join(", ", locationFilterDto.getStates())));
        }
        if (!CollectionUtils.isEmpty(locationFilterDto.getCountries())) {
            parts.add("countries=%s".formatted(String.join(", ", locationFilterDto.getCountries())));
        }
        if (!CollectionUtils.isEmpty(locationFilterDto.getRegions())) {
            parts.add("regions=%s".formatted(String.join(", ", locationFilterDto.getRegions())));
        }
        return CollectionUtils.isEmpty(parts) ? null : String.join(", ", parts);
    }

}
