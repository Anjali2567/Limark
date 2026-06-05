package ai.leadplus.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class OtpResendTooSoonException extends RuntimeException {

    public OtpResendTooSoonException(long intervalSeconds) {
        super("Please wait " + intervalSeconds + " seconds before requesting a new OTP");
    }
}
