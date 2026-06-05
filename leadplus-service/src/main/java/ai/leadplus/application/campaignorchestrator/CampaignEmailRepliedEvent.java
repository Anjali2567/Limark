package ai.leadplus.application.campaignorchestrator;

import ai.leadplus.application.campaigncontacts.CampaignContactDto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class CampaignEmailRepliedEvent extends ApplicationEvent {

    private final CampaignContactDto campaignContactDto;
    private final LocalDateTime repliedAt = LocalDateTime.now();

    public CampaignEmailRepliedEvent(
            Object source,
            CampaignContactDto campaignContactDto
    ) {
        super(source);
        this.campaignContactDto = campaignContactDto;
    }
}

