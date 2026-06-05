package ai.leadplus.application.leadcontact;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LeadContactCreatedEvent extends ApplicationEvent {

    private final LeadContactDto leadContactDto;

    public LeadContactCreatedEvent(Object source, LeadContactDto leadContactDto) {
        super(source);
        this.leadContactDto = leadContactDto;
    }
}
