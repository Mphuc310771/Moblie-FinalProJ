package com.smartbudget.app.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.smartbudget.app.ai.impl.GeminiServiceImpl;
import com.smartbudget.app.ai.impl.GroqServiceImpl;

/**
 * Enterprise AI Provider Manager using Strategy Pattern.
 * Acts as the Context class that delegates to the current AI strategy.
 * 
 * <h2>Design Pattern:</h2>
 * Strategy Pattern - Allows switching AI providers at runtime without
 * changing client code.
 * 
 * <h2>Features:</h2>
 * <ul>
 *   <li>Multi-provider support (Gemini, Groq)</li>
 *   <li>Automatic provider fallback on failure</li>
 *   <li>Provider persistence across sessions</li>
 *   <li>Unified API for all providers</li>
 * </ul>
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * AIProviderManager manager = AIProviderManager.getInstance(context);
 * 
 * // Chat with current provider
 * manager.chat("How to save money?", new AICallback() {
 *     @Override
 *     public void onSuccess(String response) {
 *         // Handle response
 *     }
 *     
 *     @Override
 *     public void onError(String error, int code) {
 *         // Handle error
 *     }
 * });
 * 
 * // Switch provider
 * manager.setProvider(AIProvider.GEMINI);
 * }</pre>
 * 
 * @author SmartBudget Development Team
 * @version 2.0 - Enterprise Edition
 */
public class AIProviderManager {

    private static final String TAG = "AIProviderManager";
    
    // ==================== SINGLETON ====================
    
    private static volatile AIProviderManager instance;
    
    /**
     * Gets the singleton instance.
     *
     * @param context Android context
     * @return AIProviderManager instance
     */
    @NonNull
    public static AIProviderManager getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (AIProviderManager.class) {
                if (instance == null) {
                    instance = new AIProviderManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    // ==================== PROVIDER ENUM ====================

    /**
     * Available AI providers.
     */
    public enum AIProvider {
        /** Google Gemini (primary) */
        GEMINI("gemini", "Google Gemini"),
        /** Groq (fast, open-source models) */
        GROQ("groq", "Groq");

        private final String id;
        private final String displayName;

        AIProvider(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }

        @Nullable
        public static AIProvider fromId(@Nullable String id) {
            if (id == null) return null;
            for (AIProvider p : values()) {
                if (p.id.equals(id)) return p;
            }
            return null;
        }
    }

    // ==================== CONFIGURATION ====================
    
    private static final String PREFS_NAME = "ai_provider_manager";
    private static final String KEY_PROVIDER = "current_provider";
    private static final String KEY_MODEL = "current_model";
    
    private final SharedPreferences prefs;
    private final AIConfig config;
    
    // ==================== STRATEGY STATE ====================
    
    private AIService currentService;
    private AIProvider currentProvider;
    private GeminiServiceImpl geminiService;
    private GroqServiceImpl groqService;

    // ==================== CONSTRUCTOR ====================

    private AIProviderManager(@NonNull Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.config = AIConfig.getInstance();
        
        // Initialize services
        this.geminiService = new GeminiServiceImpl(config);
        this.groqService = new GroqServiceImpl(config);
        
        // Load saved provider or detect best available
        loadProvider();
        
        Log.d(TAG, "Initialized: " + config.getConfigSummary());
    }

    // ==================== PROVIDER MANAGEMENT ====================

    /**
     * Gets the current AI provider.
     *
     * @return Current provider
     */
    @NonNull
    public AIProvider getCurrentProvider() {
        return currentProvider;
    }

    /**
     * Gets the current AI service.
     *
     * @return Current service implementation
     */
    @NonNull
    public AIService getCurrentService() {
        return currentService;
    }

    /**
     * Sets the AI provider.
     *
     * @param provider Provider to use
     * @return true if switch was successful
     */
    public boolean setProvider(@NonNull AIProvider provider) {
        AIService service = getServiceForProvider(provider);
        if (service != null && service.isConfigured()) {
            currentProvider = provider;
            currentService = service;
            saveProvider();
            Log.d(TAG, "Switched to provider: " + provider.getDisplayName());
            return true;
        }
        Log.w(TAG, "Provider not configured: " + provider);
        return false;
    }

    /**
     * Gets all available (configured) providers.
     *
     * @return Array of available providers
     */
    @NonNull
    public AIProvider[] getAvailableProviders() {
        java.util.List<AIProvider> available = new java.util.ArrayList<>();
        for (AIProvider p : AIProvider.values()) {
            AIService service = getServiceForProvider(p);
            if (service != null && service.isConfigured()) {
                available.add(p);
            }
        }
        return available.toArray(new AIProvider[0]);
    }

    /**
     * Checks if any provider is available.
     *
     * @return true if at least one provider is configured
     */
    public boolean isAnyProviderAvailable() {
        return getAvailableProviders().length > 0;
    }

    // ==================== DELEGATE METHODS ====================

    /**
     * Sends a chat message to the current AI provider.
     *
     * @param message User message
     * @param callback Response callback
     */
    public void chat(@NonNull String message, @NonNull AICallback callback) {
        ensureProvider();
        currentService.chat(message, new FallbackCallback(callback, message));
    }

    /**
     * Gets financial advice.
     *
     * @param query User's financial question
     * @param callback Response callback
     */
    public void getFinancialAdvice(@NonNull String query, @NonNull AICallback callback) {
        ensureProvider();
        currentService.getFinancialAdvice(query, new FallbackCallback(callback, query));
    }

    /**
     * Analyzes spending data.
     *
     * @param spendingData Formatted spending data
     * @param callback Response callback
     */
    public void analyzeSpending(@NonNull String spendingData, @NonNull AICallback callback) {
        ensureProvider();
        currentService.analyzeSpending(spendingData, callback);
    }

    /**
     * Suggests budget based on income.
     *
     * @param monthlyIncome Monthly income amount
     * @param callback Response callback
     */
    public void suggestBudget(double monthlyIncome, @NonNull AICallback callback) {
        ensureProvider();
        currentService.suggestBudget(monthlyIncome, callback);
    }

    /**
     * Clears conversation history.
     */
    public void clearHistory() {
        if (currentService != null) {
            currentService.clearHistory();
        }
    }

    // ==================== MODEL MANAGEMENT ====================

    /**
     * Gets current model name.
     *
     * @return Model identifier
     */
    @NonNull
    public String getCurrentModel() {
        return currentService != null ? currentService.getCurrentModel() : "unknown";
    }

    /**
     * Gets available models for current provider.
     *
     * @return Array of model identifiers
     */
    @NonNull
    public String[] getAvailableModels() {
        // Return ALL available models for selection
        java.util.List<String> allModels = new java.util.ArrayList<>();
        
        // Add Gemini Models
        allModels.add(AIConfig.DEFAULT_GEMINI_MODEL);
        java.util.Collections.addAll(allModels, AIConfig.GEMINI_FALLBACK_MODELS);
        
        // Add Groq Models
        java.util.Collections.addAll(allModels, AIConfig.GROQ_MODELS);
        
        return allModels.toArray(new String[0]);
    }

    // ==================== INTERNAL METHODS ====================

    private AIService getServiceForProvider(AIProvider provider) {
        switch (provider) {
            case GEMINI: return geminiService;
            case GROQ: return groqService;
            default: return null;
        }
    }

    private void loadProvider() {
        String savedId = prefs.getString(KEY_PROVIDER, null);
        AIProvider saved = AIProvider.fromId(savedId);
        
        // Try saved provider first
        if (saved != null && setProvider(saved)) {
            return;
        }
        
        // Auto-detect best available provider
        if (config.isGroqConfigured()) {
            setProvider(AIProvider.GROQ);
        } else if (config.isGeminiConfigured()) {
            setProvider(AIProvider.GEMINI);
        } else {
            // Fallback to Gemini even if not configured (will error on use)
            currentProvider = AIProvider.GEMINI;
            currentService = geminiService;
        }
    }

    private void saveProvider() {
        prefs.edit().putString(KEY_PROVIDER, currentProvider.getId()).apply();
    }

    private void ensureProvider() {
        if (currentService == null) {
            loadProvider();
        }
    }

    // ==================== FALLBACK CALLBACK ====================

    /**
     * Callback wrapper that implements automatic provider fallback.
     */
    private class FallbackCallback implements AICallback {
        private final AICallback delegate;
        private final String originalMessage;
        private boolean triedFallback = false;

        FallbackCallback(AICallback delegate, String originalMessage) {
            this.delegate = delegate;
            this.originalMessage = originalMessage;
        }

        @Override
        public void onSuccess(@NonNull String response) {
            delegate.onSuccess(response);
        }

        @Override
        public void onError(@NonNull String error, int errorCode) {
            // Try fallback provider once
            if (!triedFallback && errorCode != Result.ERROR_AUTH) {
                AIProvider fallback = getFallbackProvider();
                if (fallback != null && setProvider(fallback)) {
                    triedFallback = true;
                    Log.d(TAG, "Falling back to: " + fallback.getDisplayName());
                    
                    // Retry with fallback provider and prepend warning to response
                    currentService.chat(originalMessage, new AICallback() {
                        @Override
                        public void onSuccess(@NonNull String response) {
                            delegate.onSuccess("⚠️ [Auto-Switch] Gemini quá tải, đã chuyển sang " + fallback.getDisplayName() + ".\n\n" + response);
                        }

                        @Override
                        public void onError(@NonNull String err, int code) {
                            delegate.onError(err, code);
                        }
                    });
                    return;
                }
            }
            delegate.onError(error, errorCode);
        }

        private AIProvider getFallbackProvider() {
            for (AIProvider p : getAvailableProviders()) {
                if (p != currentProvider) {
                    return p;
                }
            }
            return null;
        }
    }

    // ==================== BACKWARD COMPATIBILITY ====================

    /**
     * Legacy callback interface for backward compatibility.
     * @deprecated Use {@link AICallback} instead
     */
    @Deprecated
    public interface GeminiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    /**
     * Legacy method for backward compatibility.
     * @deprecated Use {@link #getFinancialAdvice(String, AICallback)} instead
     */
    @Deprecated
    public void getFinancialAdvice(@NonNull String query, @NonNull GeminiCallback callback) {
        getFinancialAdvice(query, new AICallback() {
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
}
