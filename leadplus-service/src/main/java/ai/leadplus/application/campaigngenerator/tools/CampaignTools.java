package ai.leadplus.application.campaigngenerator.tools;

import ai.leadplus.application.campaignemails.CampaignEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CampaignTools {

    private final CampaignEmailService campaignEmailService;

    @Tool(
            name = "updateCampaignEmailSteps",
            description = "Update the number of email steps in a campaign"
    )
    public void updateCampaignEmailSteps(
            @ToolParam(description = "Id of the current campaign") String campaignId,
            @ToolParam(description = "Number of email steps to set for the campaign") int stepNumber
    ) {
        campaignEmailService.updateNoOfSequences(Long.parseLong(campaignId), stepNumber);
        log.info("Campaign with ID: {} updated to have {} email steps.", campaignId, stepNumber);
    }
}
