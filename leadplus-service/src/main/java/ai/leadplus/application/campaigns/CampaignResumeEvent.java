package ai.leadplus.application.campaigns;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CampaignResumeEvent extends ApplicationEvent {

    private final CampaignDto campaignDto;

    public CampaignResumeEvent(Object source, CampaignDto campaignDto) {
        super(source);
        this.campaignDto = campaignDto;
    }
}
