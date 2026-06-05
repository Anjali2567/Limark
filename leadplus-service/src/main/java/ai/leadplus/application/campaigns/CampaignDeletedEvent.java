package ai.leadplus.application.campaigns;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CampaignDeletedEvent extends ApplicationEvent {

    private final CampaignDto campaignDto;

    public CampaignDeletedEvent(Object source, CampaignDto campaignDto) {
        super(source);
        this.campaignDto = campaignDto;
    }
}
