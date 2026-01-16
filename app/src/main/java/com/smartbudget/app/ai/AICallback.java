package com.smartbudget.app.ai;

import androidx.annotation.NonNull;

/**
 * Callback interface for AI service responses.
 * Provides unified callback handling across all AI providers.
 * 
 * @author SmartBudget Development Team
 * @version 1.0
 */
public interface AICallback {

    /**
     * Called when AI request succeeds.
     *
     * @param response The AI response text
     */
    void onSuccess(@NonNull String response);

    /**
     * Called when AI request fails.
     *
     * @param error Error message
     * @param errorCode Error code from {@link Result}
     */
    void onError(@NonNull String error, int errorCode);

    /**
     * Convenience method for errors without code.
     *
     * @param error Error message
     */
    default void onError(@NonNull String error) {
        onError(error, Result.ERROR_UNKNOWN);
    }
}
