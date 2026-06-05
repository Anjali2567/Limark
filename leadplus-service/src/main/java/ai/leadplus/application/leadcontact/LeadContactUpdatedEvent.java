package ai.leadplus.application.leadcontact;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LeadContactUpdatedEvent extends ApplicationEvent {

    private final LeadContactDto leadContactDto;

    public LeadContactUpdatedEvent(Object source, LeadContactDto leadContactDto) {
        super(source);
        this.leadContactDto = leadContactDto;
    }
}
