package ai.leadplus.application.facts;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FactUpdateEvent extends ApplicationEvent {

    private final FactDto factDto;

    public FactUpdateEvent(Object source, FactDto factDto) {
        super(source);
        this.factDto = factDto;
    }
}