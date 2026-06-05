package ai.leadplus.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidOAuthLoginException extends RuntimeException {
    public InvalidOAuthLoginException(String message) {
        super(message);
    }
}
