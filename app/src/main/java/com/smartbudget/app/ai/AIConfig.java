package com.smartbudget.app.ai;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.smartbudget.app.BuildConfig;

/**
 * Centralized configuration for AI services.
 * Manages API keys, timeouts, URLs, and retry settings.
 * 
 * <h2>Security:</h2>
 * API keys are loaded from BuildConfig (set in gradle.properties or local.properties).
 * Never hardcode API keys in source code.
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * AIConfig config = AIConfig.getInstance();
 * String geminiKey = config.getGeminiApiKey();
 * 
 * if (config.isGeminiConfigured()) {
 *     // Use Gemini
 * }
 * }</pre>
 * 
 * @author SmartBudget Development Team
 * @version 1.0
 */
public final class AIConfig {

    private static final String TAG = "AIConfig";

    // ==================== SINGLETON ====================
    
    private static volatile AIConfig instance;
    
    /**
     * Gets the singleton instance.
     *
     * @return AIConfig instance
     */
    @NonNull
    public static AIConfig getInstance() {
        if (instance == null) {
            synchronized (AIConfig.class) {
                if (instance == null) {
                    instance = new AIConfig();
                }
            }
        }
        return instance;
    }

    // ==================== BASE URLs ====================
    
    /** Google Gemini API base URL */
    public static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/";
    
    /** Groq API base URL */
    public static final String GROQ_BASE_URL = "https://api.groq.com/";

    // ==================== DEFAULT MODELS ====================
    
    /** Default Gemini model */
    public static final String DEFAULT_GEMINI_MODEL = "gemini-2.0-flash";
    
    /** Fallback Gemini models in order of preference */
    public static final String[] GEMINI_FALLBACK_MODELS = {
        "gemini-1.5-flash",
        "gemini-1.5-flash-001",
        "gemini-pro"
    };
    
    /** Default Groq model */
    public static final String DEFAULT_GROQ_MODEL = "llama-3.3-70b-versatile";
    
    /** Available Groq models */
    public static final String[] GROQ_MODELS = {
        "llama-3.3-70b-versatile",
        "llama-3.1-70b-versatile",
        "mixtral-8x7b-32768",
        "gemma2-9b-it"
    };

    // ==================== TIMEOUTS (seconds) ====================
    
    /** Connection timeout */
    public static final int CONNECT_TIMEOUT = 30;
    
    /** Read timeout */
    public static final int READ_TIMEOUT = 60;
    
    /** Write timeout */
    public static final int WRITE_TIMEOUT = 60;

    // ==================== RETRY CONFIGURATION ====================
    
    /** Maximum retry attempts */
    public static final int MAX_RETRY_ATTEMPTS = 3;
    
    /** Base delay for exponential backoff (ms) */
    public static final long BASE_RETRY_DELAY_MS = 1000;
    
    /** Rate limit retry delay (ms) */
    public static final long RATE_LIMIT_DELAY_MS = 2000;

    // ==================== CONSTRUCTOR ====================

    private AIConfig() {
        // Private constructor for singleton
    }

    // ==================== API KEY METHODS ====================

    /**
     * Gets the Gemini API key from BuildConfig.
     *
     * @return API key or null if not configured
     */
    @Nullable
    public String getGeminiApiKey() {
        try {
            String key = BuildConfig.GEMINI_API_KEY;
            if (isValidKey(key)) {
                return key;
            }
            Log.e(TAG, "GEMINI_API_KEY not configured in gradle.properties");
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error reading GEMINI_API_KEY", e);
            return null;
        }
    }

    /**
     * Gets the Groq API key from BuildConfig.
     *
     * @return API key or null if not configured
     */
    @Nullable
    public String getGroqApiKey() {
        try {
            String key = BuildConfig.GROQ_API_KEY;
            if (isValidKey(key)) {
                return key;
            }
            Log.e(TAG, "GROQ_API_KEY not configured in gradle.properties");
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error reading GROQ_API_KEY", e);
            return null;
        }
    }

    /**
     * Checks if Gemini is properly configured.
     *
     * @return true if API key is valid
     */
    public boolean isGeminiConfigured() {
        return isValidKey(getGeminiApiKey());
    }

    /**
     * Checks if Groq is properly configured.
     *
     * @return true if API key is valid
     */
    public boolean isGroqConfigured() {
        return isValidKey(getGroqApiKey());
    }

    /**
     * Checks if any AI provider is configured.
     *
     * @return true if at least one provider is ready
     */
    public boolean isAnyProviderConfigured() {
        return isGeminiConfigured() || isGroqConfigured();
    }

    // ==================== RETRY HELPERS ====================

    /**
     * Calculates exponential backoff delay.
     *
     * @param attempt Current attempt number (0-based)
     * @return Delay in milliseconds
     */
    public long getRetryDelay(int attempt) {
        // Exponential backoff: 1s, 2s, 4s...
        long delay = (long) (BASE_RETRY_DELAY_MS * Math.pow(2, attempt));
        // Cap at 10 seconds
        return Math.min(delay, 10000);
    }

    /**
     * Checks if retry should be attempted based on error code.
     *
     * @param httpCode HTTP response code
     * @return true if retry is recommended
     */
    public boolean shouldRetry(int httpCode) {
        return httpCode == 429  // Rate limit
            || httpCode == 503  // Service unavailable
            || httpCode == 504  // Gateway timeout
            || httpCode >= 500; // Server errors
    }

    // ==================== VALIDATION ====================

    /**
     * Validates an API key.
     *
     * @param key Key to validate
     * @return true if key is non-null and non-empty
     */
    private boolean isValidKey(@Nullable String key) {
        return key != null && !key.trim().isEmpty();
    }

    // ==================== DEBUG ====================

    /**
     * Checks if running in debug mode.
     *
     * @return true if debug build
     */
    public boolean isDebugMode() {
        return BuildConfig.DEBUG;
    }

    /**
     * Gets configuration summary for logging.
     *
     * @return Configuration status string
     */
    @NonNull
    public String getConfigSummary() {
        return "AIConfig{" +
               "gemini=" + (isGeminiConfigured() ? "OK" : "NOT_SET") +
               ", groq=" + (isGroqConfigured() ? "OK" : "NOT_SET") +
               ", debug=" + isDebugMode() +
               "}";
    }
}
