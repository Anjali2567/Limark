package ai.leadplus.application.campaigns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampaignAnalyticsDto {
    private long sentEmails;
    private long openedEmails;
    private long repliedEmails;
    private double emailsOpenRate;
    private double emailsReplyRate;
}
