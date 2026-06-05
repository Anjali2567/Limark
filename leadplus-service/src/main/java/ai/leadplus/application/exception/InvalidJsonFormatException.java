package ai.leadplus.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InvalidJsonFormatException extends RuntimeException{
    public InvalidJsonFormatException(String message) {
        super(message);
    }
}
