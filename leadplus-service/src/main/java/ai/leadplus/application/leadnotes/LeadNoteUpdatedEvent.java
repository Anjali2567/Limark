package ai.leadplus.application.leadnotes;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LeadNoteUpdatedEvent extends ApplicationEvent {
    private final LeadNoteDto leadNoteDto;

    public LeadNoteUpdatedEvent(Object source, LeadNoteDto leadNoteDto) {
        super(source);
        this.leadNoteDto = leadNoteDto;
    }
}

