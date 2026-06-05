package ai.leadplus.application.campaignemails;

import ai.leadplus.application.campaigns.CampaignDto;
import ai.leadplus.application.campaigns.CampaignService;
import ai.leadplus.application.promptspecifications.PromptSpecificationDto;
import ai.leadplus.application.promptspecifications.PromptSpecificationService;
import ai.leadplus.domain.promptspecifications.PromptSpecificationType;
import ai.leadplus.infrastructure.springai.SpringAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignEmailAiService {

    private final SpringAiClient springAiClient;
    private final CampaignService campaignService;
    private final CampaignEmailService campaignEmailService;
    private final PromptSpecificationService promptSpecificationService;

    public CampaignEmailListCompletion generateEmail(String campaignId, String userPrompt) {
        CampaignDto campaignDto = campaignService.getCampaignById(Long.parseLong(campaignId));

        List<CampaignEmailDto> campaignEmailDtoList = campaignEmailService.getEmailSequencesByCampaignId(Long.parseLong(campaignId));
        String existingEmails;
        if (CollectionUtils.isEmpty(campaignEmailDtoList)) {
            existingEmails = "No existing emails.";
        } else {
            existingEmails = campaignEmailDtoList.stream()
                    .map(CampaignEmailDto::toAiPromptString)
                    .collect(Collectors.joining(",\n"));
        }

        PromptSpecificationDto promptSpecificationDto = promptSpecificationService.getLatestPromptSpecificationByType(PromptSpecificationType.CAMPAIGN_EMAIL_GENERATOR);

        String systemPromptWithContext = promptSpecificationDto.getPromptTemplate()
                .replace("{{CAMPAIGN_DETAILS}}", campaignDto.toCleanedString())
                .replace("{{CAMPAIGN_EMAILS}}", existingEmails);
        return springAiClient.getChatCompletion(
                userPrompt,
                systemPromptWithContext,
                CampaignEmailListCompletion.class
        );
    }
}
