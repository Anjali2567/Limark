package ai.leadplus.api.v1.campaigns;

import ai.leadplus.api.common.UserValidator;
import ai.leadplus.application.campaignagent.CampaignAgentService;
import ai.leadplus.application.campaignchatmemory.CampaignChatMemoryDto;
import ai.leadplus.application.campaigngenerator.CampaignGeneratorService;
import ai.leadplus.application.campaigns.*;
import ai.leadplus.application.messages.MessageDto;
import ai.leadplus.application.leads.LeadFilterCriteria;
import ai.leadplus.application.useractivitylogs.TrackUserActivity;
import ai.leadplus.domain.campaigns.CampaignStatus;
import ai.leadplus.domain.useractivitylogs.LogLevel;
import ai.leadplus.domain.useractivitylogs.UserActivityLogType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/tenants/{tenantId}/workspaces/{workspaceId}/campaigns")
@Tag(name = "Campaign APIs", description = "APIs related to Campaign operations")
public class CampaignController {

    private final CampaignGeneratorService campaignGeneratorService;
    private final CampaignService campaignService;
    private final CampaignAgentService campaignAgentService;
    private final UserValidator userValidator;
    private final CampaignSearchService campaignSearchService;

    @Operation(summary = "Campaign Generator Chat Reply")
    @PostMapping("/generator")
    @TrackUserActivity(
            type = UserActivityLogType.CAMPAIGN,
            level = LogLevel.INFO,
            message = "Generated Campaign via AI. CampaignId: #{#result.body.campaignId}"
    )
    public ResponseEntity<CampaignGeneratorResponse> generateCampaign(@PathVariable Long tenantId,
                                                                      @PathVariable Long workspaceId,
                                                                      @RequestBody CampaignGeneratorRequest campaignGeneratorRequest) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[POST] Received chat request to generate campaign : {}", campaignGeneratorRequest.getRequest());
        MessageDto messageDto = campaignGeneratorRequest.toDto();
        MessageDto responseMessageDto = campaignGeneratorService.getMessageReply(tenantId, workspaceId, Long.valueOf(userId), messageDto);
        CampaignGeneratorResponse campaignGeneratorResponse = CampaignGeneratorResponse.fromDto(responseMessageDto);
        log.info("[POST] Campaign generated successfully");
        return ResponseEntity.ok(campaignGeneratorResponse);
    }

    @Operation(summary = "Campaign Agent Chat Reply")
    @PostMapping("/agent")
    @TrackUserActivity(
            type = UserActivityLogType.CAMPAIGN,
            level = LogLevel.INFO,
            message = "Campaign Agent replied. CampaignId: #{#result.body.campaignId}"
    )
    public ResponseEntity<CampaignAgentResponse> getReply(@PathVariable Long tenantId,
                                                          @PathVariable Long workspaceId,
                                                          @RequestBody CampaignGeneratorRequest campaignGeneratorRequest) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[POST] Received chat request: {}", campaignGeneratorRequest.getRequest());
        MessageDto messageDto = campaignGeneratorRequest.toDto();
        MessageDto responseMessageDto = campaignAgentService.getMessageReply(tenantId, workspaceId, Long.valueOf(userId), messageDto);
        CampaignAgentResponse campaignGeneratorResponse = CampaignAgentResponse.fromDto(responseMessageDto);
        log.info("[POST] Response generated successfully");
        return ResponseEntity.ok(campaignGeneratorResponse);
    }

    @Operation(summary = "Get Campaign by ID")
    @GetMapping("/agent/{campaignChatMemoryId}")
    @TrackUserActivity(
            type = UserActivityLogType.CAMPAIGN,
            level = LogLevel.INFO,
            message = "Viewed Campaign from ChatMemoryId: #{#campaignChatMemoryId}"
    )
    public ResponseEntity<CampaignResponse> getCampaignChatMemoryId(@PathVariable Long tenantId,
                                                                    @PathVariable Long workspaceId,
                                                                    @PathVariable Long campaignChatMemoryId) {
        log.info("[GET] Received request to get Campaign by CampaignChatMemoryId: {}", campaignChatMemoryId);
        CampaignInfoDto campaignInfoDto = campaignService.getCampaignFromChatMemoryId(tenantId, campaignChatMemoryId);
        CampaignResponse response = CampaignResponse.fromDto(campaignInfoDto);
        log.info("[GET] Returning Campaign with CampaignChatMemoryId: {}", campaignChatMemoryId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Campaign Target Criteria")
    @PutMapping("/agent/{campaignChatMemoryId}/target-criteria")
    @TrackUserActivity(
            type = UserActivityLogType.CAMPAIGN,
            level = LogLevel.INFO,
            message = "Updated Target Criteria for CampaignChatMemoryId: #{#campaignChatMemoryId}"
    )
    public ResponseEntity<TargetingCriteriaResponse> updateCampaignFilters(@PathVariable Long tenantId,
                                                                           @PathVariable Long workspaceId,
                                                                           @PathVariable Long campaignChatMemoryId,
                                                                           @RequestBody LeadFilterCriteria leadFilterCriteria) {
        log.info("[PUT] Updating campaign target criteria for campaignId: {}", campaignChatMemoryId);
        CampaignChatMemoryDto campaignChatMemoryDto = campaignAgentService.updateCampaignFilters(campaignChatMemoryId, leadFilterCriteria);
        TargetingCriteriaResponse response = TargetingCriteriaResponse.fromDto(campaignChatMemoryDto.getTargetingCriteria());
        log.info("[PUT] Campaign target criteria updated successfully for campaignId: {}", campaignChatMemoryId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create campaign from Lead Search Results")
    @PostMapping("/search-results")
    @TrackUserActivity(
            type = UserActivityLogType.CAMPAIGN,
            level = LogLevel.INFO,
            message = "Created Campaign from Lead Search. CampaignId: #{#result.body.id}"
    )
    public ResponseEntity<CampaignDetailsResponse> createCampaignFromLeadSearch(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @RequestBody CampaignSearchResultRequest campaignSearchResultRequest) {
        log.info("[POST] Received request to create campaign from lead search results");
        CampaignDto campaignDto = campaignService.createCampaignForSearch(
                tenantId,
                workspaceId,
                campaignSearchResultRequest.getContactIds(),
                campaignSearchResultRequest.getLeadFilterCriteria()
        );
        CampaignDetailsResponse response = CampaignDetailsResponse.fromDto(campaignDto);
        log.info("[POST] Campaign created successfully from lead search results");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Proceed Campaign")
    @PostMapping("/agent/{campaignChatMemoryId}/proceed")
    @TrackUserActivity(
            type = UserActivityLogType.CAMPAIGN,
            level = LogLevel.INFO,
            message = "Proceeded Campaign from ChatMemoryId: #{#campaignChatMemoryId}, CampaignId: #{#result.body.id}"
    )
    public ResponseEntity<CampaignDetailsResponse> proceedCampaign(@PathVariable Long tenantId,
                                                                   @PathVariable Long workspaceId,
                                                                   @PathVariable Long campaignChatMemoryId) {
        log.info("[POST] Received request to proceed campaign for CampaignChatMemoryId: {}", campaignChatMemoryId);
        CampaignDto campaignDto = campaignService.proceedCampaignFromChatMemory(campaignChatMemoryId);
        CampaignDetailsResponse response = CampaignDetailsResponse.fromDto(campaignDto);
        log.info("[POST] Proceeded campaign successfully for CampaignChatMemoryId: {}", campaignChatMemoryId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get All Campaigns")
    @GetMapping
    @TrackUserActivity(
            type = UserActivityLogType.CAMPAIGN,
            level = LogLevel.INFO,
            message = "Viewed Campaign list in Workspace: #{#workspaceId}"
    )
    public ResponseEntity<Page<CampaignListResponse>> getAllCampaigns(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false) List<CampaignStatus> campaignStatuses,
            @RequestParam(required = false) List<String> industries,
            Pageable pageable) {
        log.info("[GET] Received request to get all Campaigns by WorkspaceId: {}", workspaceId);
        Page<CampaignListDto> campaignListDtoPage = campaignSearchService.searchCampaigns(
                tenantId, workspaceId, query, campaignStatuses, industries, pageable
        );
        Page<CampaignListResponse> response = campaignListDtoPage.map(CampaignListResponse::fromDto);
        log.info("[GET] Retrieved {} Campaigns:", response.getSize());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Campaign by ID")
    @GetMapping("/{campaignId}")
    @TrackUserActivity(
            type = UserActivityLogType.CAMPAIGN,
            level = LogLevel.INFO,
            message = "Viewed Campaign with ID: #{#campaignId}"
    )
    public ResponseEntity<CampaignResponse> getCampaignById(@PathVariable Long tenantId,
                                                            @PathVariable Long workspaceId,
                                                            @PathVariable Long campaignId) {
        log.info("[GET] Received request to get Campaign by ID: {}", campaignId);
        CampaignInfoDto campaignInfoDto = campaignService.getCampaignDataById(tenantId, campaignId);
        CampaignResponse response = CampaignResponse.fromDto(campaignInfoDto);
        log.info("[GET] Returning Campaign with ID: {}", campaignId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Basic Campaign Info")
    @GetMapping("/{campaignId}/details")
    @TrackUserActivity(
            type = UserActivityLogType.CAMPAIGN,
            level = LogLevel.INFO,
            message = "Viewed Campaign Details for ID: #{#campaignId}"
    )
    public ResponseEntity<CampaignDetailsResponse> getBasicCampaignInfo(@PathVariable Long tenantId,
                                                                        @PathVariable Long workspaceId,
                                                                        @PathVariable Long campaignId) {
        log.info("[GET] Received request to get Basic Campaign Info by ID: {}", campaignId);
        CampaignDto campaignDto = campaignService.getCampaignById(campaignId);
        CampaignDetailsResponse response = CampaignDetailsResponse.fromDto(campaignDto);
        log.info("[GET] Returning Basic Campaign Info with ID: {}", campaignId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Launch Campaign")
    @PostMapping("/{campaignId}/launch")
    @TrackUserActivity(
            type = UserActivityLogType.CAMPAIGN,
            level = LogLevel.INFO,
            message = "Launched Campaign with ID: #{#campaignId}"
    )
    public ResponseEntity<Void> launchCampaign(@PathVariable Long tenantId,
                                               @PathVariable Long workspaceId,
                                               @PathVariable Long campaignId) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[POST] Launching campaign with ID: {}", campaignId);
        campaignService.launchCampaignById(Long.parseLong(userId), workspaceId, campaignId);
        log.info("[POST] Campaign launched successfully with ID: {}", campaignId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Pause Campaign")
    @PutMapping("/{campaignId}/pause")
    @TrackUserActivity(
            type = UserActivityLogType.CAMPAIGN,
            level = LogLevel.INFO,
            message = "Paused Campaign with ID: #{#campaignId}"
    )
    public ResponseEntity<Void> pauseCampaign(@PathVariable Long tenantId,
                                              @PathVariable Long workspaceId,
                                              @PathVariable Long campaignId) {
        log.info("[PUT] Pausing campaign with ID: {}", campaignId);
        campaignService.pauseCampaignById(campaignId);
        log.info("[PUT] Campaign paused successfully with ID: {}", campaignId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Resume Campaign")
    @PutMapping("/{campaignId}/resume")
    @TrackUserActivity(
            type = UserActivityLogType.CAMPAIGN,
            level = LogLevel.INFO,
            message = "Resumed Campaign with ID: #{#campaignId}"
    )
    public ResponseEntity<Void> resumeCampaign(@PathVariable Long tenantId,
                                               @PathVariable Long workspaceId,
                                               @PathVariable Long campaignId) {
        log.info("[PUT] Resuming campaign with ID: {}", campaignId);
        campaignService.resumeCampaignById(campaignId);
        log.info("[PUT] Campaign resumed successfully with ID: {}", campaignId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get Analytics for all Campaigns")
    @GetMapping("/analytics")
    @TrackUserActivity(
            type = UserActivityLogType.CAMPAIGN,
            level = LogLevel.INFO,
            message = "Viewed Campaign Analytics for Workspace: #{#workspaceId}"
    )
    public ResponseEntity<CampaignAnalyticsResponse> getCampaignAnalytics(@PathVariable Long tenantId,
                                                                          @PathVariable Long workspaceId) {
        log.info("[GET] Received request to get Analytics for all Campaigns in Workspace Id: {}", workspaceId);
        CampaignAnalyticsDto campaignAnalytics = campaignService.getFullCampaignAnalytics(workspaceId);
        CampaignAnalyticsResponse campaignAnalyticsResponse = CampaignAnalyticsResponse.fromDto(campaignAnalytics);
        log.info("[GET] Analytics Response Generated successfully");
        return ResponseEntity.ok(campaignAnalyticsResponse);
    }

    @Operation(summary = "Get Analytics for a Campaign by ID")
    @GetMapping("{campaignId}/analytics")
    @TrackUserActivity(
            type = UserActivityLogType.CAMPAIGN,
            level = LogLevel.INFO,
            message = "Viewed Analytics for Campaign ID: #{#campaignId}"
    )
    public ResponseEntity<CampaignAnalyticsResponse> getCampaignAnalyticsForCampaignId(@PathVariable Long tenantId,
                                                                                       @PathVariable Long workspaceId,
                                                                                       @PathVariable Long campaignId) {
        log.info("[GET] Received request to get Analytics for Campaign with ID: {}", campaignId);
        CampaignAnalyticsDto campaignAnalytics = campaignService.getCampaignAnalyticsByCampaignId(campaignId);
        CampaignAnalyticsResponse campaignAnalyticsResponse = CampaignAnalyticsResponse.fromDto(campaignAnalytics);
        log.info("[GET] Analytics Response Generated for Campaign Id: {} successfully", campaignId);
        return ResponseEntity.ok(campaignAnalyticsResponse);
    }

    @Operation(summary = "Update Campaign Details by Id")
    @PutMapping("/{id}")
    @TrackUserActivity(
            type = UserActivityLogType.CAMPAIGN,
            level = LogLevel.INFO,
            message = "Updated Campaign Details. CampaignId: #{#result.body.id}"
    )
    public ResponseEntity<CampaignDetailsResponse> updateCampaignById(
            @PathVariable Long workspaceId,
            @PathVariable Long id,
            @Valid @RequestBody CampaignRequest campaignRequest) {
        log.info("[PUT] Received request to update Campaign by ID: {}", id);
        CampaignDto campaignDto = campaignService.updateCampaignDetails(id, campaignRequest.toDto());
        CampaignDetailsResponse response = CampaignDetailsResponse.fromDto(campaignDto);
        log.info("[PUT] Returning Campaign with ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Campaign Recipients by Id")
    @PutMapping("/{id}/recipients")
    @TrackUserActivity(
            type = UserActivityLogType.CAMPAIGN,
            level = LogLevel.INFO,
            message = "Updated Campaign Recipients. CampaignId: #{#result.body.id}"
    )
    public ResponseEntity<CampaignDetailsResponse> updateCampaignRecipients(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @PathVariable Long id,
            @Valid @RequestBody CampaignRecipientsRequest campaignRecipientsRequest) {
        log.info("[PUT] Received request to update Campaign Recipients by ID: {}", id);
        CampaignDto campaignDto = campaignService.updateCampaignRecipients(id, campaignRecipientsRequest.toDto());
        CampaignDetailsResponse response = CampaignDetailsResponse.fromDto(campaignDto);
        log.info("[PUT] Campaign Recipients updated for Campaign with ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Sending Mailbox for campaign")
    @PutMapping("/{id}/mailbox/{mailboxId}")
    @TrackUserActivity(
            type = UserActivityLogType.CAMPAIGN,
            level = LogLevel.INFO,
            message = "Updated Sending Mailbox for Campaign ID: #{#result.body.id}"
    )
    public ResponseEntity<CampaignDetailsResponse> updateCampaignSendingMailbox(
            @PathVariable Long tenantId,
            @PathVariable Long workspaceId,
            @PathVariable Long id,
            @PathVariable Long mailboxId) {
        String userId = userValidator.getAuthenticatedUserId();
        log.info("[PUT] Received request to update Sending Mailbox for Campaign with ID: {}", id);
        CampaignDto campaignDto = campaignService.updateCampaignSendingMailbox(id, mailboxId, workspaceId, Long.parseLong(userId));
        CampaignDetailsResponse response = CampaignDetailsResponse.fromDto(campaignDto);
        log.info("[PUT] Campaign SendingMailbox updated for Campaign with ID: {}", id);
        return ResponseEntity.ok(response);
    }
}
