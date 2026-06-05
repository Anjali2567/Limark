package ai.leadplus.application.campaigns;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CampaignPausedEvent extends ApplicationEvent {

    private final CampaignDto campaignDto;

    public CampaignPausedEvent(Object source, CampaignDto campaignDto) {
        super(source);
        this.campaignDto = campaignDto;
    }
}
