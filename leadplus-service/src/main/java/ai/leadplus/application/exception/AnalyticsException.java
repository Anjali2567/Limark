package ai.leadplus.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_GATEWAY)
public class AnalyticsException extends RuntimeException {

    public AnalyticsException(String message, Throwable cause) {
        super(message, cause);
    }
}