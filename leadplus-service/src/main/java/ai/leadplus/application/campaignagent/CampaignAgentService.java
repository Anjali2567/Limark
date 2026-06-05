package ai.leadplus.application.campaignagent;

import ai.leadplus.application.campaignagent.tools.AgentLeadCountAggregationService;
import ai.leadplus.application.campaignagent.tools.LeadSearchAgentService;
import ai.leadplus.application.campaignagent.tools.LeadSearchAgentTools;
import ai.leadplus.application.campaignchatmemory.CampaignChatMemoryDto;
import ai.leadplus.application.campaignchatmemory.CampaignChatMemoryService;
import ai.leadplus.application.campaigncontacts.CampaignContactService;
import ai.leadplus.application.campaigngenerator.CampaignPromptFilterContextService;
import ai.leadplus.application.campaigngenerator.tools.CampaignTools;
import ai.leadplus.application.campaigns.CampaignDto;
import ai.leadplus.application.campaigns.CampaignProceededEvent;
import ai.leadplus.application.common.TargetingCriteriaDto;
import ai.leadplus.application.contactoutreachstatuses.ContactOutreachStatusService;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.leads.ContactLeadSearchService;
import ai.leadplus.application.leads.LeadDto;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.application.messages.MessageDto;
import ai.leadplus.application.messages.MessageService;
import ai.leadplus.application.common.utils.FileReader;
import ai.leadplus.application.common.utils.PromptTemplateContextInjector;
import ai.leadplus.application.vendordatapacks.VendorDataPackService;
import ai.leadplus.domain.messages.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignAgentService {

    private final ChatClient chatClient;
    private final LeadSearchAgentTools leadSearchAgentTools;
    private final CampaignTools campaignTools;
    private final MessageService messageService;
    private final CampaignChatMemoryService campaignChatMemoryService;
    private final LeadSearchAgentService leadSearchAgentService;
    private final ContactLeadSearchService contactLeadSearchService;
    private final VendorDataPackService vendorDataPackService;
    private final CampaignContactService campaignContactService;
    private final ContactOutreachStatusService contactOutreachStatusService;
    private final AgentLeadCountAggregationService agentLeadCountAggregationService;
    private final CampaignPromptFilterContextService campaignPromptFilterContextService;

    private static final String TENANT_FILTER_CONTEXT_PLACEHOLDER = "{{TENANT_FILTER_CONTEXT}}";
    private static final String CAMPAIGN_AGENT_PROMPT = FileReader
            .readFileContentFromClasspath("prompts/campaign-agent.md");

    private static final int PROCEED_BATCH_SIZE   = 500;
    private static final int PROCEED_MAX_CONTACTS = 10_000;

    public CampaignAgentCompletion getChatCompletion(Long tenantId, Long workspaceId, MessageDto messageDto) {
        boolean isNewConversation = !StringUtils.hasText(messageDto.getConversationId());

        CampaignChatMemoryDto campaignChatMemoryDto = isNewConversation
                ? campaignChatMemoryService.createBasicCampaignChatMemory(tenantId, workspaceId)
                : campaignChatMemoryService.findCampaignMemoryByConversationId(workspaceId, messageDto.getConversationId());

        String conversationId = isNewConversation ? "-" : messageDto.getConversationId();

        String industryAccessBlock = buildIndustryAccessBlock(tenantId);
        String appliedFilters = campaignChatMemoryDto.getLeadFilter() != null
                ? campaignChatMemoryDto.getLeadFilter().toString()
                : "None";

        String tenantFilterContext = campaignPromptFilterContextService.buildContextBlock(tenantId);
        String promptWithContext = PromptTemplateContextInjector.injectRequired(
                CAMPAIGN_AGENT_PROMPT,
                TENANT_FILTER_CONTEXT_PLACEHOLDER,
                tenantFilterContext,
                "CAMPAIGN_AGENT"
        );
        String systemPromptWithFilters = promptWithContext
                .replace("{{CAMPAIGN_ID}}", String.valueOf(campaignChatMemoryDto.getId()))
                .replace("{{APPLIED_FILTERS}}", appliedFilters)
                .replace("{{ACCESSIBLE_INDUSTRIES}}", industryAccessBlock);

        CampaignAgentCompletion campaignAgentCompletion = chatClient
                .prompt()
                .tools(leadSearchAgentTools, campaignTools)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(messageDto.getRequest())
                .system(systemPromptWithFilters)
                .call()
                .entity(CampaignAgentCompletion.class);

        if (campaignAgentCompletion.getCampaignId() == null) {
            campaignAgentCompletion.setCampaignId(campaignChatMemoryDto.getId());
        }
        return campaignAgentCompletion;
    }

    public MessageDto getMessageReply(Long tenantId, Long workspaceId, Long userId, MessageDto messageDto) {
        boolean isNewConversation = !StringUtils.hasText(messageDto.getConversationId());

        LocalDateTime searchAtBefore = null;
        if (!isNewConversation) {
            CampaignChatMemoryDto before = campaignChatMemoryService
                    .findCampaignMemoryByConversationId(workspaceId, messageDto.getConversationId());
            if (before != null) {
                searchAtBefore = before.getLastSearchAt();
            }
        }

        CampaignAgentCompletion chatCompletion = getChatCompletion(tenantId, workspaceId, messageDto);
        Long campaignChatMemoryId = chatCompletion.getCampaignId();

        messageDto.setTenantId(tenantId);
        messageDto.setWorkspaceId(workspaceId);
        messageDto.setUserId(userId);
        messageDto.setResponse(chatCompletion.getResponse());

        CampaignChatMemoryDto campaignChatMemoryDto = null;
        boolean searchPerformed = false;

        if (campaignChatMemoryId != null) {
            messageDto.setCampaignId(campaignChatMemoryId);
            campaignChatMemoryDto = campaignChatMemoryService.getCampaignChatMemoryById(campaignChatMemoryId);
            TargetingCriteriaDto targetingCriteriaDto = campaignChatMemoryDto.getTargetingCriteria();
            messageDto.setCampaignState(targetingCriteriaDto);

            LocalDateTime searchAtAfter = campaignChatMemoryDto.getLastSearchAt();
            searchPerformed = searchAtAfter != null && !searchAtAfter.equals(searchAtBefore);
        }

        messageDto.setSearchPerformed(searchPerformed);
        MessageDto savedMessage = messageService.createMessage(messageDto, MessageType.CAMPAIGN_AGENT);

        if (campaignChatMemoryDto != null) {
            TargetingCriteriaDto targetingCriteriaDto = campaignChatMemoryDto.getTargetingCriteria() != null
                    ? campaignChatMemoryDto.getTargetingCriteria()
                    : new TargetingCriteriaDto();
            targetingCriteriaDto.setConversationId(savedMessage.getConversationId());
            campaignChatMemoryDto.setTargetingCriteria(targetingCriteriaDto);
            campaignChatMemoryDto.setName(chatCompletion.getCampaignName());
            campaignChatMemoryDto.setIndustry(chatCompletion.getCampaignIndustry());
            campaignChatMemoryService.updateCampaignChatMemory(campaignChatMemoryDto);
        }
        return savedMessage;
    }

    public CampaignChatMemoryDto updateCampaignFilters(Long campaignChatMemoryId, LeadFilterCriteria leadFilterCriteria) {
        if (leadFilterCriteria == null) {
            throw new BadRequestException("Lead filters cannot be null");
        }
        CampaignChatMemoryDto campaignChatMemoryDto =
                campaignChatMemoryService.getCampaignChatMemoryById(campaignChatMemoryId);
        return leadSearchAgentService.updateCampaignFiltersAndCount(campaignChatMemoryDto, leadFilterCriteria);
    }

    @EventListener
    public void handleCampaignProceeded(CampaignProceededEvent event) {
        CampaignDto campaign   = event.getCampaignDto();
        LeadFilterCriteria storedFilter = event.getLeadFilter();
        Integer contactLimit   = event.getContactLimit();
        var gatedInfo          = event.getGatedInfo();
        var vendorAccess       = event.getVendorAccess();

        if (storedFilter == null) {
            log.warn("CampaignProceededEvent: no filter for campaignId={}. No contacts to populate.",
                    campaign.getId());
            return;
        }

        log.info("Populating contacts for campaignId={} | contactLimit={}", campaign.getId(), contactLimit);

        Set<Long> ineligibleContactIds =
                contactOutreachStatusService.getIneligibleContactIds(campaign.getTenantId());

        try {
            List<Long> resolvedCompanyIds = agentLeadCountAggregationService.resolveCompanyIds(
                    campaign.getTenantId(), storedFilter, gatedInfo, vendorAccess, ineligibleContactIds);

            if (CollectionUtils.isEmpty(resolvedCompanyIds)) {
                log.info("No matching companies for campaignId={}. Zero contacts populated.", campaign.getId());
                campaignContactService.updateContactIdsOfCampaign(
                        campaign.getTenantId(), campaign.getId(), new LinkedHashSet<>(), false);
                return;
            }

            log.info("Resolved {} matching companies for campaignId={}", resolvedCompanyIds.size(), campaign.getId());

            int hardLimit = (contactLimit != null && contactLimit > 0)
                    ? Math.min(contactLimit, PROCEED_MAX_CONTACTS)
                    : PROCEED_MAX_CONTACTS;

            LeadFilterCriteria contactFilter = LeadFilterCriteria.builder()
                    .titles(storedFilter.getTitles())
                    .seniority(storedFilter.getSeniority())
                    .departments(storedFilter.getDepartments())
                    .cities(storedFilter.getCities())
                    .states(storedFilter.getStates())
                    .countries(storedFilter.getCountries())
                    .excludeContactIds(ineligibleContactIds)
                    .aggregateTechSearch(false) // no further company-level filtering needed
                    .build();

            Set<Long> collectedContactIds = new LinkedHashSet<>();
            int pageNumber = 0;

            while (collectedContactIds.size() < hardLimit) {
                int remaining = hardLimit - collectedContactIds.size();
                int batchSize = Math.min(PROCEED_BATCH_SIZE, remaining);

                Page<LeadDto> batch = contactLeadSearchService.searchLeads(
                        campaign.getTenantId(),
                        contactFilter,
                        null,
                        resolvedCompanyIds,
                        gatedInfo,
                        vendorAccess,
                        PageRequest.of(pageNumber, batchSize));

                if (batch.isEmpty()) break;

                batch.getContent().stream()
                        .map(LeadDto::getId)
                        .forEach(collectedContactIds::add);

                if (!batch.hasNext()) break;

                pageNumber++;
            }

            campaignContactService.updateContactIdsOfCampaign(
                    campaign.getTenantId(), campaign.getId(), collectedContactIds, false);

            log.info("Populated {} contacts for campaignId={}", collectedContactIds.size(), campaign.getId());

        } catch (Exception e) {
            log.error("Failed to populate contacts for campaignId={}", campaign.getId(), e);
        }
    }

    private String buildIndustryAccessBlock(Long tenantId) {
        return vendorDataPackService.getVendorAccessForAuthenticatedUser(tenantId)
                .map(access -> {
                    List<String> industries = access.getAccessibleIndustryNames();
                    List<String> segments   = access.getNamedVendorSegments();
                    boolean hasIndustries   = !CollectionUtils.isEmpty(industries);
                    boolean hasSegments     = !CollectionUtils.isEmpty(segments);
                    if (!hasIndustries && !hasSegments) return "none";
                    StringBuilder result = new StringBuilder();
                    if (hasIndustries) result.append("Industries: ").append(String.join(", ", industries));
                    if (hasSegments) {
                        if (result.length() > 0) result.append(" | ");
                        result.append("Segments: ").append(String.join(", ", segments));
                    }
                    return result.toString();
                })
                .orElse("none");
    }

}