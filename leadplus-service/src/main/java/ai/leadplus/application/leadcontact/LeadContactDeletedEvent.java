package ai.leadplus.application.leadcontact;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LeadContactDeletedEvent extends ApplicationEvent {

    private final LeadContactDto leadContactDto;

    public LeadContactDeletedEvent(Object source, LeadContactDto leadContactDto) {
        super(source);
        this.leadContactDto = leadContactDto;
    }
}
