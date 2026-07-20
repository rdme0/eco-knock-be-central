package jnu.econovation.ecoknockbecentral.common.exception.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jnu.econovation.ecoknockbecentral.auth.core.passport.PassportException;
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse;
import jnu.econovation.ecoknockbecentral.common.exception.client.ClientException;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;
import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "서버에서 예상치 못한 오류가 발생했습니다.";

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        String parameterName = ex.getParameterName();
        String message = "필요로 하는 파라미터 -> " + parameterName + " 이(가) 없습니다.";

        log.warn("Missing parameter: {}", parameterName);

        return handleExceptionInternal(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        StringBuilder errorMessage = new StringBuilder();

        String firstError = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(ObjectError::getDefaultMessage)
                .orElse("잘못된 요청입니다.");

        errorMessage.append(firstError);

        // for 디버깅
        if (log.isDebugEnabled()) {
            ex.getBindingResult().getAllErrors().forEach(error -> {
                if (error instanceof FieldError fieldError) {
                    log.debug("Validation error - Field: {}, Value: {}, Message: {}",
                            fieldError.getField(), fieldError.getRejectedValue(),
                            error.getDefaultMessage());
                } else {
                    log.debug("Validation error - Message: {}", error.getDefaultMessage());
                }
            });
        }

        return handleExceptionInternal(ErrorCode.INVALID_INPUT_VALUE, errorMessage.toString());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        ResponseEntity<Object> clientResponse = handleClientCauseOrNull(ex);
        if (clientResponse != null) {
            return clientResponse;
        }

        Throwable cause = ex.getCause();

        if (cause instanceof JsonProcessingException jsonEx) {
            log.warn("JSON parsing error: {}", jsonEx.getMessage());
            return failureEntity(ErrorCode.INVALID_INPUT_VALUE, resolveJsonErrorMessage(jsonEx));
        }

        log.warn("HTTP message not readable: {}", ex.getMessage());

        return failureEntity(ErrorCode.INVALID_INPUT_VALUE, "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {
        ResponseEntity<Object> clientResponse = handleClientCauseOrNull(ex);
        if (clientResponse != null) {
            return clientResponse;
        }

        String message = String.format("파라미터 '%s'의 값 '%s'이(가) 올바르지 않습니다.",
                ex.getName(), ex.getValue());
        log.warn("Type mismatch for parameter: {} with value: {}", ex.getName(), ex.getValue());

        return handleExceptionInternal(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> handleBindException(BindException ex) {
        ResponseEntity<Object> clientResponse = handleClientCauseOrNull(ex);
        if (clientResponse != null) {
            return clientResponse;
        }

        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("잘못된 요청입니다.");

        log.warn("Bind exception: {}", message);

        return handleExceptionInternal(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<Object> handleJsonProcessingException(
            JsonProcessingException ex
    ) {
        ResponseEntity<Object> clientResponse = handleClientCauseOrNull(ex);
        if (clientResponse != null) {
            return clientResponse;
        }

        log.warn("JSON processing error: {}", ex.getMessage());

        String message = "JSON 처리 중 오류가 발생했습니다: " + ex.getOriginalMessage();

        return handleExceptionInternal(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e) {

        log.warn("IllegalArgumentException 발생 : {}", e.getMessage());

        return handleExceptionInternal(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
    }


    @ExceptionHandler(ClientException.class)
    public ResponseEntity<CommonResponse<Void>> handleClientException(ClientException e) {

        log.warn("클라이언트 예외 발생: Code={}, Message={}", e.getErrorCode().getCode(), e.getMessage());

        return handleClientExceptionInternal(e);
    }

    @ExceptionHandler(PassportException.class)
    public ResponseEntity<Object> handlePassportException(PassportException e) {
        ErrorCode errorCode = e.getHttpStatus() == HttpStatus.BAD_REQUEST
                ? ErrorCode.INVALID_INPUT_VALUE
                : ErrorCode.UNAUTHORIZED;

        log.warn("Passport 예외 발생: Status={}, Message={}", e.getHttpStatus(), e.getMessage());

        return handleExceptionInternal(errorCode, e.getMessage());
    }

    @ExceptionHandler(BeanInstantiationException.class)
    public ResponseEntity<Object> handleBeanInstantiationException(BeanInstantiationException e) {
        ResponseEntity<Object> clientResponse = handleClientCauseOrNull(e);
        if (clientResponse != null) {
            return clientResponse;
        }

        log.warn("Bean instantiation exception: {}", e.getMessage());

        return handleExceptionInternal(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
    }


    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<Object> handleServerException(InternalServerException e) {
        ErrorCode errorCode = e.getErrorCode();

        log.error("서버에 의한 오류 발생: Code={}, Message={}", errorCode.getCode(), e.getMessage(), e);

        if (e.getErrorCode().getStatus() != HttpStatus.INTERNAL_SERVER_ERROR) {
            return handleExceptionInternal(errorCode, e.getMessage());
        }

        return handleExceptionInternal(errorCode, INTERNAL_SERVER_ERROR_MESSAGE);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception e) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        log.error("서버에 의한 오류 발생: ", e);

        return handleExceptionInternal(errorCode, INTERNAL_SERVER_ERROR_MESSAGE);
    }


    private ResponseEntity<CommonResponse<Void>> handleClientExceptionInternal(ClientException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(CommonResponse.failure(e.getErrorCode(), e.getMessage()));
    }

    private ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode, String message) {
        return failureEntity(errorCode, message);
    }

    private ResponseEntity<Object> failureEntity(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(CommonResponse.failure(errorCode, message));
    }

    private @Nullable ResponseEntity<Object> handleClientCauseOrNull(Throwable ex) {
        ClientException clientException = findCause(ex, ClientException.class);
        if (clientException == null) {
            return null;
        }

        return failureEntity(clientException.getErrorCode(), clientException.getMessage());
    }

    private <T extends Throwable> T findCause(Throwable throwable, Class<T> type) {
        Throwable current = throwable;

        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }

            current = current.getCause();
        }

        return null;
    }

    private String resolveJsonErrorMessage(JsonProcessingException ex) {
        return switch (ex) {
            case InvalidFormatException invalidFormatEx -> "필드 '%s'의 값이 올바르지 않습니다: %s"
                    .formatted(fieldName(invalidFormatEx), invalidFormatEx.getValue());
            case MismatchedInputException mismatchedEx -> "필드 '%s'의 타입이 맞지 않습니다.".formatted(fieldName(mismatchedEx));
            case JsonMappingException mappingEx ->
                    "필드 '%s'에 문제가 있습니다: %s".formatted(fieldName(mappingEx), causeMessage(mappingEx));
            default -> "잘못된 JSON 형식입니다.";
        };
    }

    private String fieldName(JsonMappingException ex) {
        return ex.getPath().isEmpty()
                ? "unknown"
                : ex.getPath().getFirst().getFieldName();
    }

    private String causeMessage(Throwable ex) {
        return ex.getCause() == null ? "" : ex.getCause().getMessage();
    }
}
