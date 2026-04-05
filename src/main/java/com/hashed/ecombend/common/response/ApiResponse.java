package com.hashed.ecombend.common.response;

import lombok.Getter;

/**
 * Unified API response envelope every endpoint returns this schema.
 * Success: { "success": true,  "message": "...", "data": { ... } }
 * Error:   { "success": false, "message": "...", "data": null     }
 *
 * @param <T> The type of the response data payload
 */
@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * Successful response with a data payload.
     *
     * @param message
     * @param data
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Successful response with no data.
     *
     * @param message
     */
    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /**
     * Error response. ideally created inside RestExceptionHandler.
     *
     * @param message
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
