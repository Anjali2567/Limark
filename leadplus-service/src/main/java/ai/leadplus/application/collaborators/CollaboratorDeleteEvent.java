package ai.leadplus.application.collaborators;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CollaboratorDeleteEvent extends ApplicationEvent {

    private final CollaboratorDto collaboratorDto;

    public CollaboratorDeleteEvent(Object source, CollaboratorDto collaboratorDto) {
        super(source);
        this.collaboratorDto = collaboratorDto;
    }
}
