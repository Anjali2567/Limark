package ai.leadplus.application.leadcompany;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LeadCompanyUpdatedEvent extends ApplicationEvent {

    private final LeadCompanyDto leadCompanyDto;

    public LeadCompanyUpdatedEvent(Object source, LeadCompanyDto leadCompanyDto) {
        super(source);
        this.leadCompanyDto = leadCompanyDto;
    }
}
