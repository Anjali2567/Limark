package ai.leadplus.application.collaborators;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CollaboratorUpdateEvent extends ApplicationEvent {

    private final CollaboratorDto collaboratorDto;

    public CollaboratorUpdateEvent(Object source, CollaboratorDto collaboratorDto) {
        super(source);
        this.collaboratorDto = collaboratorDto;
    }
}
