package ai.leadplus.application.campaignorchestrator;

import ai.leadplus.application.campaigncontacts.CampaignContactInfoDto;
import ai.leadplus.application.campaignemails.CampaignEmailDto;
import ai.leadplus.application.campaigns.CampaignDto;
import ai.leadplus.application.mailboxes.MailboxDto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class CampaignEmailSentEvent extends ApplicationEvent {

    private final Long tenantId;
    private final Long workspaceId;
    private final MailboxDto mailboxDto;
    private final CampaignDto campaignDto;
    private final CampaignEmailDto campaignEmailDto;
    private final CampaignContactInfoDto campaignContactInfoDto;
    private final LocalDateTime sentAt = LocalDateTime.now();

    public CampaignEmailSentEvent(
            Object source,
            Long tenantId,
            Long workspaceId,
            MailboxDto mailboxDto,
            CampaignDto campaignDto,
            CampaignEmailDto campaignEmailDto,
            CampaignContactInfoDto campaignContactInfoDto
    ) {
        super(source);
        this.tenantId = tenantId;
        this.workspaceId = workspaceId;
        this.mailboxDto = mailboxDto;
        this.campaignDto = campaignDto;
        this.campaignEmailDto = campaignEmailDto;
        this.campaignContactInfoDto = campaignContactInfoDto;
    }
}
