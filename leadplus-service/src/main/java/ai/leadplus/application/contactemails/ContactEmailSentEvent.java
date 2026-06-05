package ai.leadplus.application.contactemails;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class ContactEmailSentEvent extends ApplicationEvent {

    private final ContactEmailDto contactEmailDto;
    private final LocalDateTime sentAt = LocalDateTime.now();

    public ContactEmailSentEvent(Object source, ContactEmailDto contactEmailDto) {
        super(source);
        this.contactEmailDto = contactEmailDto;
    }
}

