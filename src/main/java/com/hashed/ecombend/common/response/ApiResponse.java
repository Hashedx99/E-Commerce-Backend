package com.hashed.ecombend.common.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * Unified API response wrapper. Every endpoint returns this shape.
 * Consistent response format = frontend clients never handle different structures.
 * Success: { "success": true,  "message": "Product created", "data": { ... } }
 * Error:   { "success": false, "message": "Product not found", "data": null  }
 *
 * @param <T> The type of the response data payload
 */

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
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
     * Creates a successful response with data and message.
     *
     * @param message success message
     * @param data    The response payload
     * @param <T>     Type of the data
     * @return success response with data
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Creates a successful response with only a message (no data payload).
     * Use for operations like delete, logout, password change.
     *
     * @param message success message
     * @param <T>     Type placeholder
     * @return success response without data
     */
    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /**
     * Creates an error response. Typically used in exception handlers.
     *
     * @param message error message
     * @param <T>     Type placeholder
     * @return error response without data
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
