package ai.leadplus.application.facts;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FactDeleteEvent extends ApplicationEvent {

    private final FactDto factDto;

    public FactDeleteEvent(Object source, FactDto factDto) {
        super(source);
        this.factDto = factDto;
    }
}