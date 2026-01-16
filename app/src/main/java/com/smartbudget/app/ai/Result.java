package com.smartbudget.app.ai;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Generic Result wrapper for operations that can succeed or fail.
 * Provides type-safe handling of success/error states without exceptions.
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * Result<String> result = aiService.chat("Hello");
 * 
 * if (result.isSuccess()) {
 *     String response = result.getData();
 *     // Handle success
 * } else {
 *     String error = result.getError();
 *     int code = result.getErrorCode();
 *     // Handle error
 * }
 * 
 * // Or use functional style:
 * result.onSuccess(data -> handleSuccess(data))
 *       .onError((error, code) -> handleError(error, code));
 * }</pre>
 * 
 * @param <T> The type of data on success
 * @author SmartBudget Development Team
 * @version 1.0
 */
public final class Result<T> {

    // ==================== ERROR CODES ====================
    
    /** No error */
    public static final int ERROR_NONE = 0;
    /** Network connection error */
    public static final int ERROR_NETWORK = 1;
    /** API rate limit exceeded */
    public static final int ERROR_RATE_LIMIT = 2;
    /** Invalid API key */
    public static final int ERROR_AUTH = 3;
    /** Invalid request/input */
    public static final int ERROR_INVALID_REQUEST = 4;
    /** Server error (5xx) */
    public static final int ERROR_SERVER = 5;
    /** Unknown/unexpected error */
    public static final int ERROR_UNKNOWN = 99;

    // ==================== FIELDS ====================
    
    private final boolean success;
    @Nullable private final T data;
    @Nullable private final String error;
    private final int errorCode;

    // ==================== CONSTRUCTORS ====================

    private Result(boolean success, @Nullable T data, @Nullable String error, int errorCode) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.errorCode = errorCode;
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Creates a successful result with data.
     *
     * @param data The success data (non-null)
     * @param <T> Type of data
     * @return Success result
     */
    @NonNull
    public static <T> Result<T> success(@NonNull T data) {
        return new Result<>(true, data, null, ERROR_NONE);
    }

    /**
     * Creates an error result with message.
     *
     * @param error Error message
     * @param <T> Type parameter (unused for errors)
     * @return Error result
     */
    @NonNull
    public static <T> Result<T> error(@NonNull String error) {
        return new Result<>(false, null, error, ERROR_UNKNOWN);
    }

    /**
     * Creates an error result with message and code.
     *
     * @param error Error message
     * @param errorCode Error code (use ERROR_* constants)
     * @param <T> Type parameter
     * @return Error result
     */
    @NonNull
    public static <T> Result<T> error(@NonNull String error, int errorCode) {
        return new Result<>(false, null, error, errorCode);
    }

    /**
     * Creates a network error result.
     *
     * @param message Error details
     * @param <T> Type parameter
     * @return Network error result
     */
    @NonNull
    public static <T> Result<T> networkError(@NonNull String message) {
        return new Result<>(false, null, "Lỗi mạng: " + message, ERROR_NETWORK);
    }

    /**
     * Creates a rate limit error result.
     *
     * @param <T> Type parameter
     * @return Rate limit error result
     */
    @NonNull
    public static <T> Result<T> rateLimitError() {
        return new Result<>(false, null, "Đã vượt giới hạn API. Vui lòng thử lại sau.", ERROR_RATE_LIMIT);
    }

    /**
     * Creates an authentication error result.
     *
     * @param <T> Type parameter
     * @return Auth error result
     */
    @NonNull
    public static <T> Result<T> authError() {
        return new Result<>(false, null, "API key không hợp lệ hoặc chưa được cấu hình.", ERROR_AUTH);
    }

    // ==================== GETTERS ====================

    /**
     * Checks if operation was successful.
     *
     * @return true if success, false if error
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Checks if operation failed.
     *
     * @return true if error, false if success
     */
    public boolean isError() {
        return !success;
    }

    /**
     * Gets the success data.
     *
     * @return Data or null if error
     */
    @Nullable
    public T getData() {
        return data;
    }

    /**
     * Gets the error message.
     *
     * @return Error message or null if success
     */
    @Nullable
    public String getError() {
        return error;
    }

    /**
     * Gets the error code.
     *
     * @return Error code (ERROR_NONE if success)
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Gets data or throws exception if error.
     *
     * @return Data (never null for success)
     * @throws IllegalStateException if result is error
     */
    @NonNull
    public T getDataOrThrow() {
        if (!success || data == null) {
            throw new IllegalStateException("Result is error: " + error);
        }
        return data;
    }

    /**
     * Gets data or default value if error.
     *
     * @param defaultValue Value to return on error
     * @return Data or default
     */
    @NonNull
    public T getDataOrDefault(@NonNull T defaultValue) {
        return success && data != null ? data : defaultValue;
    }

    // ==================== FUNCTIONAL METHODS ====================

    /**
     * Executes action if result is success.
     *
     * @param action Action to execute with data
     * @return This result for chaining
     */
    @NonNull
    public Result<T> onSuccess(@NonNull SuccessCallback<T> action) {
        if (success && data != null) {
            action.onSuccess(data);
        }
        return this;
    }

    /**
     * Executes action if result is error.
     *
     * @param action Action to execute with error info
     * @return This result for chaining
     */
    @NonNull
    public Result<T> onError(@NonNull ErrorCallback action) {
        if (!success) {
            action.onError(error != null ? error : "Unknown error", errorCode);
        }
        return this;
    }

    // ==================== CALLBACK INTERFACES ====================

    /**
     * Callback for success case.
     *
     * @param <T> Data type
     */
    public interface SuccessCallback<T> {
        void onSuccess(@NonNull T data);
    }

    /**
     * Callback for error case.
     */
    public interface ErrorCallback {
        void onError(@NonNull String error, int errorCode);
    }

    // ==================== OBJECT METHODS ====================

    @NonNull
    @Override
    public String toString() {
        if (success) {
            return "Result.Success{data=" + data + "}";
        } else {
            return "Result.Error{error='" + error + "', code=" + errorCode + "}";
        }
    }
}
