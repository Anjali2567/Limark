package ai.leadplus.api.v1.campaigns.campaigncontacts;

import ai.leadplus.application.campaigncontacts.CampaignContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/v1/tenants/{tenantId}/workspaces/{workspaceId}/campaigns/{campaignId}/contacts")
@RequiredArgsConstructor
@Tag(name = "Campaign Contact APIs", description = "APIs related to Campaign Contact operations")
public class CampaignContactController {

    private final CampaignContactService campaignContactService;

    @Operation(summary = "Update Campaign Contacts")
    @PutMapping
    public ResponseEntity<Void> updateCampaignContacts(@PathVariable Long tenantId,
                                                       @PathVariable String workspaceId,
                                                       @PathVariable Long campaignId,
                                                       @RequestBody CampaignContactsUpdateRequest request) {
        log.info("[PUT] Received request to update contacts for Campaign ID: {}", campaignId);
        campaignContactService.updateCampaignContacts(campaignId, request.getSelectedCampaignContactIds(), request.getExcludedCampaignContactIds());
        log.info("[PUT] Updated contacts for Campaign ID: {}", campaignId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add contacts to a campaign")
    @PostMapping
    public ResponseEntity<Void> addContactsToCampaign(@PathVariable Long tenantId,
                                                      @PathVariable String workspaceId,
                                                      @PathVariable Long campaignId,
                                                      @RequestBody @Valid AddCampaignContactsRequest request) {
        log.info("[POST] Received request to add {} contacts to Campaign ID: {}", request.getContactIds().size(), campaignId);
        campaignContactService.addContactsToCampaign(tenantId, campaignId, request.getContactIds());
        log.info("[POST] Added contacts to Campaign ID: {}", campaignId);
        return ResponseEntity.noContent().build();
    }
}
