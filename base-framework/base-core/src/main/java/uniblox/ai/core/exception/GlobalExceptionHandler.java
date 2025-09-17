package uniblox.ai.core.exception;

import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger;          // Injected from LoggerConfig
    private final MessageSource messageSource;

    public GlobalExceptionHandler(Logger logger, MessageSource messageSource) {
        this.logger = logger;
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, Locale locale) {
        String localizedMessage = messageSource.getMessage(ex.getErrorCode(), null, locale);
        logger.error("API Exception: {}", localizedMessage, ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getErrorCode(), localizedMessage));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, Locale locale) {
        String errorCode = "error.validation.failed";
        String localizedMessage = messageSource.getMessage(errorCode, null, locale);

        logger.warn("Validation failed: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errorCode, localizedMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, Locale locale) {
        String errorCode = "error.internal";
        String localizedMessage = messageSource.getMessage(errorCode, null, locale);

        logger.error("Unexpected error", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(errorCode, localizedMessage));
    }

    public record ErrorResponse(String code, String message) {}
}
