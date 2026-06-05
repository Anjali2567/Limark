package ai.leadplus.application.campaigns;

import ai.leadplus.application.campaigncontacts.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignAnalyticsService {

    public CampaignAnalyticsDto calculateWorkspaceAnalytics(List<CampaignContactDto> contacts) {
        if (CollectionUtils.isEmpty(contacts)) {
            return CampaignAnalyticsDto.builder()
                    .sentEmails(0L)
                    .emailsOpenRate(0.0)
                    .emailsReplyRate(0.0)
                    .build();
        }

        long totalSent = 0;
        long totalOpened = 0;
        long totalReplied = 0;

        for (CampaignContactDto contact : contacts) {
            List<EmailDataDto> emailDataDtoList = contact.getEmailData();
            if (!CollectionUtils.isEmpty(emailDataDtoList)) {
                for (EmailDataDto emailData : emailDataDtoList) {
                    if (emailData.getEmailDeliveryStatus() != null) {
                        totalSent++;
                        switch (emailData.getEmailDeliveryStatus()) {
                            case OPENED -> totalOpened++;
                            case REPLIED -> {
                                totalOpened++;
                                totalReplied++;
                            }
                        }
                    }
                }
            }
        }

        double openRate = totalSent == 0 ? 0.0 : 1.0 * totalOpened / totalSent * 100;
        double replyRate = totalSent == 0 ? 0.0 : 1.0 * totalReplied / totalSent * 100;

        return CampaignAnalyticsDto.builder()
                .sentEmails(totalSent)
                .openedEmails(totalOpened)
                .repliedEmails(totalReplied)
                .emailsOpenRate(openRate)
                .emailsReplyRate(replyRate)
                .build();
    }
}
