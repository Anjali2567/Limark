package ai.leadplus.application.contactoutreachstatuses;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class ContactUnsubscribedEvent extends ApplicationEvent {

    private final Long tenantId;
    private final Long contactId;
    private final List<Long> campaignIds;

    public ContactUnsubscribedEvent(Object source, Long tenantId, Long contactId, List<Long> campaignIds) {
        super(source);
        this.tenantId = tenantId;
        this.contactId = contactId;
        this.campaignIds = campaignIds;
    }
}
