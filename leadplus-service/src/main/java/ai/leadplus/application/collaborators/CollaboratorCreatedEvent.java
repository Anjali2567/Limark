package ai.leadplus.application.collaborators;

import ai.leadplus.application.users.UserDto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class CollaboratorCreatedEvent extends ApplicationEvent {

    private final CollaboratorDto collaboratorDto;
    private final UserDto inviter;
    private final UserDto invitee;
    private final List<String> collaboratorUserIds;

    public CollaboratorCreatedEvent(Object source,
                                    CollaboratorDto collaboratorDto,
                                    UserDto inviter,
                                    UserDto invitee,
                                    List<String> collaboratorUserIds) {
        super(source);
        this.collaboratorDto = collaboratorDto;
        this.inviter = inviter;
        this.invitee = invitee;
        this.collaboratorUserIds = collaboratorUserIds;
    }
}
