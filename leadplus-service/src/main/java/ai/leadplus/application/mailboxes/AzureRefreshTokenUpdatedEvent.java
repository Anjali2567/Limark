package ai.leadplus.application.mailboxes;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AzureRefreshTokenUpdatedEvent extends ApplicationEvent {

    private final String mailboxId;
    private final String newRefreshToken;

    public AzureRefreshTokenUpdatedEvent(Object source, String mailboxId, String newRefreshToken) {
        super(source);
        this.mailboxId = mailboxId;
        this.newRefreshToken = newRefreshToken;
    }
}
