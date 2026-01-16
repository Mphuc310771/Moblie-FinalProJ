package com.smartbudget.app.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.smartbudget.app.ai.AICallback;
import com.smartbudget.app.ai.AIProviderManager;

/**
 * Backward compatibility alias for GeminiHelper.
 * 
 * @deprecated This class is deprecated. Use {@link AIProviderManager} instead.
 * 
 * <h2>Migration Guide:</h2>
 * <pre>{@code
 * // OLD (deprecated)
 * GeminiHelper helper = GeminiHelper.getInstance(context);
 * helper.getFinancialAdvice("query", new GeminiHelper.GeminiCallback() {...});
 * 
 * // NEW (recommended)
 * AIProviderManager manager = AIProviderManager.getInstance(context);
 * manager.getFinancialAdvice("query", new AICallback() {...});
 * }</pre>
 * 
 * @author SmartBudget Development Team
 * @version 3.0 (Deprecated - Use AIProviderManager)
 */
@Deprecated
public class GeminiHelper {

    private final AIProviderManager manager;

    /**
     * Gets the singleton instance.
     * @deprecated Use {@link AIProviderManager#getInstance(Context)} instead
     */
    @Deprecated
    public static GeminiHelper getInstance(@NonNull Context context) {
        return new GeminiHelper(context);
    }

    private GeminiHelper(@NonNull Context context) {
        this.manager = AIProviderManager.getInstance(context);
    }

    /**
     * Legacy callback interface.
     * @deprecated Use {@link AICallback} instead
     */
    @Deprecated
    public interface GeminiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    /**
     * Gets financial advice.
     * @deprecated Use {@link AIProviderManager#getFinancialAdvice(String, AICallback)}
     */
    @Deprecated
    public void getFinancialAdvice(@NonNull String query, @NonNull GeminiCallback callback) {
        manager.getFinancialAdvice(query, new AICallback() {
            @Override
            public void onSuccess(@NonNull String response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(@NonNull String error, int errorCode) {
                callback.onError(error);
            }
        });
    }

    /**
     * Clears chat history.
     * @deprecated Use {@link AIProviderManager#clearHistory()}
     */
    @Deprecated
    public void clearHistory() {
        manager.clearHistory();
    }

    /**
     * Gets current model.
     * @deprecated Use {@link AIProviderManager#getCurrentModel()}
     */
    @Deprecated
    public String getCurrentModel() {
        return manager.getCurrentModel();
    }
}
