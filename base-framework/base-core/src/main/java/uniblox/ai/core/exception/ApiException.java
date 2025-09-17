package uniblox.ai.core.exception;

public class ApiException extends RuntimeException {
    private final String errorCode;

    public ApiException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public ApiException(String errorCode, Throwable cause) {
        super(errorCode, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
