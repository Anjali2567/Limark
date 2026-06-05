package ai.leadplus.application.campaigns;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CampaignCompletedEvent extends ApplicationEvent {

    private final CampaignDto campaignDto;

    public CampaignCompletedEvent(Object source, CampaignDto campaignDto) {
        super(source);
        this.campaignDto = campaignDto;
    }
}
