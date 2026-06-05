package ai.leadplus.application.facts;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FactCreatedEvent extends ApplicationEvent {

    private final FactDto factDto;

    public FactCreatedEvent(Object source, FactDto factDto) {
        super(source);
        this.factDto = factDto;
    }
}