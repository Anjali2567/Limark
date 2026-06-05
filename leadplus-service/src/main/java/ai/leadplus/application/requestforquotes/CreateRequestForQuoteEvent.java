package ai.leadplus.application.requestforquotes;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CreateRequestForQuoteEvent extends ApplicationEvent {

    private final RequestForQuoteDto requestForQuoteDto;
    private final Long vendorId;

    public CreateRequestForQuoteEvent(Object source, RequestForQuoteDto requestForQuoteDto, Long vendorId) {
        super(source);
        this.requestForQuoteDto = requestForQuoteDto;
        this.vendorId = vendorId;
    }

}
