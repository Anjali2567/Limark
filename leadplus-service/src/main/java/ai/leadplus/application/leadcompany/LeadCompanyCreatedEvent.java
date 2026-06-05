package ai.leadplus.application.leadcompany;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LeadCompanyCreatedEvent extends ApplicationEvent {

    private final LeadCompanyDto leadCompanyDto;

    public LeadCompanyCreatedEvent(Object source, LeadCompanyDto leadCompanyDto) {
        super(source);
        this.leadCompanyDto = leadCompanyDto;
    }
}
