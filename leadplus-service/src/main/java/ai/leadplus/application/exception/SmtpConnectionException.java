package ai.leadplus.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SmtpConnectionException extends RuntimeException {

    public SmtpConnectionException(String message) {
        super(message);
    }
}