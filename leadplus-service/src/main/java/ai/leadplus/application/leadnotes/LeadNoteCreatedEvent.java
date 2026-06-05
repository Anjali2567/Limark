package ai.leadplus.application.leadnotes;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LeadNoteCreatedEvent extends ApplicationEvent {
    private final LeadNoteDto leadNoteDto;

    public LeadNoteCreatedEvent(Object source, LeadNoteDto leadNoteDto) {
        super(source);
        this.leadNoteDto = leadNoteDto;
    }
}
