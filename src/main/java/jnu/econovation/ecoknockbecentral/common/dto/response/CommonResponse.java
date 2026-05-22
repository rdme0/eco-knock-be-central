package jnu.econovation.ecoknockbecentral.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommonResponse<T>(
        boolean isSuccess,
        String message,
        String errorCode,
        T result
) {

    private static final String SUCCESS_MESSAGE = "success";

    public static CommonResponse<Void> emptySuccess() {
        return new CommonResponse<>(true, SUCCESS_MESSAGE, null, null);
    }

    public static <T> CommonResponse<T> success(T result) {
        return new CommonResponse<>(true, SUCCESS_MESSAGE, null, result);
    }

    public static CommonResponse<Void> failure(ErrorCode errorCode) {
        return new CommonResponse<>(
                false,
                errorCode.getMessage(),
                errorCode.getCode(),
                null
        );
    }

    public static CommonResponse<Void> failure(ErrorCode errorCode, String customMessage) {
        return new CommonResponse<>(
                false,
                customMessage,
                errorCode.getCode(),
                null
        );
    }
}