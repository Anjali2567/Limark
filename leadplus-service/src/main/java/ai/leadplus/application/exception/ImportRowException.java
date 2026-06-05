package ai.leadplus.application.exception;

import lombok.Getter;

@Getter
public class ImportRowException extends RuntimeException {

    private final String errorCode;

    public ImportRowException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}