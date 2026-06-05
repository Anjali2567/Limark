package ai.leadplus.application.users;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserCreatedEvent extends ApplicationEvent {

    private final UserDto userDto;

    public UserCreatedEvent(Object source, UserDto userDto) {
        super(source);
        this.userDto = userDto;
    }
}
