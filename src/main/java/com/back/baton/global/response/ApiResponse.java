package com.back.baton.global.response;

import com.back.baton.global.response.code.SuccessCode;
import com.back.baton.global.response.code.ErrorCode;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                SuccessCode.OK.getCode(),
                SuccessCode.OK.getMessage(),
                data
        );
    }

    public static <T> ApiResponse<T> success(SuccessCode successCode, T data) {
        return new ApiResponse<>(
                true,
                successCode.getCode(),
                successCode.getMessage(),
                data
        );
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return new ApiResponse<>(
                false,
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, T data) {
        return new ApiResponse<>(
                false,
                errorCode.getCode(),
                errorCode.getMessage(),
                data
        );
    }
}