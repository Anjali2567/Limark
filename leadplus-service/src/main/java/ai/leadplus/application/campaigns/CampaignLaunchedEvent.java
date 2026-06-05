package ai.leadplus.application.campaigns;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CampaignLaunchedEvent extends ApplicationEvent {

    private final CampaignDto campaignDto;

    public CampaignLaunchedEvent(Object source, CampaignDto campaignDto) {
        super(source);
        this.campaignDto = campaignDto;
    }
}
