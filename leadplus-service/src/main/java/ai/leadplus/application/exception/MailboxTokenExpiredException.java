package ai.leadplus.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class MailboxTokenExpiredException extends RuntimeException {
    public MailboxTokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
