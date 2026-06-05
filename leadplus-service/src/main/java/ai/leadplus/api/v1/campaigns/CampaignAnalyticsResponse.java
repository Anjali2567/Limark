package ai.leadplus.api.v1.campaigns;

import ai.leadplus.application.campaigns.CampaignAnalyticsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampaignAnalyticsResponse {
    private long sentEmails;
    private long openedEmails;
    private long repliedEmails;
    private double emailsOpenRate;
    private double emailsReplyRate;

    public static CampaignAnalyticsResponse fromDto(CampaignAnalyticsDto campaignAnalyticsDto) {
        return CampaignAnalyticsResponse.builder()
                .sentEmails(campaignAnalyticsDto.getSentEmails())
                .openedEmails(campaignAnalyticsDto.getOpenedEmails())
                .repliedEmails(campaignAnalyticsDto.getRepliedEmails())
                .emailsOpenRate(campaignAnalyticsDto.getEmailsOpenRate())
                .emailsReplyRate(campaignAnalyticsDto.getEmailsReplyRate())
                .build();
    }
}
