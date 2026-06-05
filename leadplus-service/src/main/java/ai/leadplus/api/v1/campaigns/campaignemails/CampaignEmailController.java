package ai.leadplus.api.v1.campaigns.campaignemails;

import ai.leadplus.application.campaignemails.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/tenants/{tenantId}/workspaces/{workspaceId}/campaigns/{campaignId}/emails")
@RequiredArgsConstructor
@Tag(name = "Campaign Email APIs", description = "APIs related to Campaign Email operations")
public class CampaignEmailController {

    private final CampaignEmailService campaignEmailService;
    private final CampaignEmailAiService campaignEmailAiService;

    @Operation(summary = "Configure Campaign Email Sequences")
    @PostMapping("/configure")
    public ResponseEntity<Void> configureEmailSequences(@PathVariable Long tenantId,
                                                        @PathVariable Long workspaceId,
                                                        @PathVariable Long campaignId) {
        log.info("[POST] Received request to configure email sequences for Campaign ID: {}", campaignId);
        campaignEmailService.configureEmailSequences(campaignId);
        log.info("[POST] Email sequences configured for Campaign ID: {}", campaignId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get Campaign Emails by Campaign ID")
    @GetMapping
    public ResponseEntity<List<CampaignEmailResponse>> getCampaignEmails(@PathVariable Long tenantId,
                                                                         @PathVariable Long workspaceId,
                                                                         @PathVariable Long campaignId) {
        log.info("[GET] Received request to get Campaign Emails for Campaign ID: {}", campaignId);
        List<CampaignEmailDto> campaignEmailDtoList = campaignEmailService.getEmailSequencesByCampaignId(campaignId);
        List<CampaignEmailResponse> responseList = campaignEmailDtoList.stream()
                .map(CampaignEmailResponse::fromDto)
                .toList();
        log.info("[GET] Returning {} Campaign Emails for Campaign ID: {}", responseList.size(), campaignId);
        return ResponseEntity.ok(responseList);
    }

    @Operation(summary = "Update Campaign Email by ID")
    @PutMapping("/{emailId}")
    public ResponseEntity<CampaignEmailResponse> updateCampaignEmail(@PathVariable Long tenantId,
                                                                     @PathVariable Long workspaceId,
                                                                     @PathVariable Long campaignId,
                                                                     @PathVariable Long emailId,
                                                                     @RequestBody CampaignEmailRequest emailRequest) {
        log.info("[PUT] Received request to update Campaign Email ID: {} for Campaign ID: {}", emailId, campaignId);
        CampaignEmailDto campaignEmailDto = campaignEmailService.updateCampaignEmail(emailId, emailRequest.toDto());
        CampaignEmailResponse response = CampaignEmailResponse.fromDto(campaignEmailDto);
        log.info("[PUT] Updated Campaign Email ID: {} for Campaign ID: {}", emailId, campaignId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create Basic Campaign Email")
    @PostMapping
    public ResponseEntity<CampaignEmailResponse> createBasicCampaignEmail(@PathVariable Long tenantId,
                                                                          @PathVariable Long workspaceId,
                                                                          @PathVariable Long campaignId) {
        log.info("[POST] Received request to create Campaign Email for Campaign ID: {}", campaignId);
        CampaignEmailDto campaignEmailDto = campaignEmailService.createBasicCampaignEmail(campaignId);
        CampaignEmailResponse response = CampaignEmailResponse.fromDto(campaignEmailDto);
        log.info("[POST] Created Campaign Email ID: {} for Campaign ID: {}", response.getId(), campaignId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Delete Campaign Email by ID")
    @DeleteMapping("/{emailId}")
    public ResponseEntity<Void> deleteCampaignEmail(@PathVariable Long tenantId,
                                                    @PathVariable Long workspaceId,
                                                    @PathVariable Long campaignId,
                                                    @PathVariable Long emailId) {
        log.info("[DELETE] Received request to delete CampaignEmail with Id: {}", emailId);
        campaignEmailService.deleteCampaignEmail(campaignId, emailId);
        log.info("[DELETE] Deleted CampaignEmail with id: {}", emailId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Generate Campaign Email Content using AI")
    @PostMapping("/generate")
    public ResponseEntity<List<CampaignEmailCompletion>> generateCampaignEmail(@PathVariable Long tenantId,
                                                                               @PathVariable Long workspaceId,
                                                                               @PathVariable Long campaignId,
                                                                               @RequestBody @Valid CampaignEmailGeneratorRequest request) {
        log.info("[POST] Received request to generate Campaign Email for Campaign ID: {}", campaignId);
        CampaignEmailListCompletion completion = campaignEmailAiService.generateEmail(String.valueOf(campaignId), request.getUserPrompt());
        List<CampaignEmailCompletion> campaignEmails = completion.getCampaignEmails();
        log.info("[POST] Campaign Email generation initiated for Campaign ID: {}", campaignId);
        return ResponseEntity.ok(campaignEmails);
    }

}
