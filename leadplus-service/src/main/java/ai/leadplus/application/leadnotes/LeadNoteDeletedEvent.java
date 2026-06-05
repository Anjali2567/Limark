package ai.leadplus.application.leadnotes;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LeadNoteDeletedEvent extends ApplicationEvent {
    private final LeadNoteDto leadNoteDto;

    public LeadNoteDeletedEvent(Object source, LeadNoteDto leadNoteDto) {
        super(source);
        this.leadNoteDto = leadNoteDto;
    }
}

