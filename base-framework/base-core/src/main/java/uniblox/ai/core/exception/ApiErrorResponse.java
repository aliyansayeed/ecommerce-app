package uniblox.ai.core.exception;

public record ApiErrorResponse(
        String code,
        String message,
        String details
) {}
