package ai.leadplus.application.vendors;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class VendorApprovedEvent extends ApplicationEvent {

    private final VendorDto vendor;

    public VendorApprovedEvent(Object source, VendorDto vendor) {
        super(source);
        this.vendor = vendor;
    }
}
