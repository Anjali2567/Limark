package ai.leadplus.application.campaigns;

import ai.leadplus.domain.campaigns.CampaignRepository;
import ai.leadplus.domain.campaigns.CampaignStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignAssignmentService {

    private final CampaignRepository campaignRepository;

    private final static List<CampaignStatus> ACTIVE_CAMPAIGN_STATUSES = List.of(
            CampaignStatus.PENDING_APPROVAL,
            CampaignStatus.APPROVED,
            CampaignStatus.RUNNING,
            CampaignStatus.PAUSED
    );

    public boolean isSendingMailboxAssigned(Long mailboxId) {
        return campaignRepository.existsBySendingMailboxIdAndStatusIn(mailboxId, ACTIVE_CAMPAIGN_STATUSES);
    }
}
