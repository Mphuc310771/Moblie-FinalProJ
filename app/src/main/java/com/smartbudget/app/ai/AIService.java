package com.smartbudget.app.ai;

import androidx.annotation.NonNull;

/**
 * Strategy interface for AI service providers.
 * Defines the contract that all AI implementations must follow.
 * 
 * <h2>Strategy Pattern:</h2>
 * This interface enables swapping AI providers at runtime without
 * changing client code. Implementations include:
 * <ul>
 *   <li>{@code GeminiServiceImpl} - Google Gemini AI</li>
 *   <li>{@code GroqServiceImpl} - Groq (Llama, Mixtral)</li>
 * </ul>
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * AIService service = new GeminiServiceImpl(config);
 * service.chat("Hello", new AICallback() {
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
 * }</pre>
 * 
 * @author SmartBudget Development Team
 * @version 1.0
 */
public interface AIService {

    // ==================== PROVIDER INFO ====================

    /**
     * Gets the provider name for display purposes.
     *
     * @return Provider display name (e.g., "Gemini", "Groq")
     */
    @NonNull
    String getProviderName();

    /**
     * Gets the current model identifier.
     *
     * @return Model ID (e.g., "gemini-2.0-flash", "llama-3.3-70b-versatile")
     */
    @NonNull
    String getCurrentModel();

    /**
     * Checks if this service is properly configured and ready.
     *
     * @return true if service can make requests
     */
    boolean isConfigured();

    // ==================== CHAT METHODS ====================

    /**
     * Sends a chat message to the AI.
     * Financial context is automatically added.
     *
     * @param message User message
     * @param callback Response callback
     */
    void chat(@NonNull String message, @NonNull AICallback callback);

    /**
     * Gets financial advice based on user query.
     *
     * @param query User's financial question
     * @param callback Response callback
     */
    void getFinancialAdvice(@NonNull String query, @NonNull AICallback callback);

    // ==================== ANALYSIS METHODS ====================

    /**
     * Analyzes spending data and provides insights.
     *
     * @param spendingData Formatted spending data
     * @param callback Response callback
     */
    void analyzeSpending(@NonNull String spendingData, @NonNull AICallback callback);

    /**
     * Suggests a budget based on income.
     *
     * @param monthlyIncome Monthly income amount
     * @param callback Response callback
     */
    void suggestBudget(double monthlyIncome, @NonNull AICallback callback);

    // ==================== LIFECYCLE ====================

    /**
     * Clears conversation history.
     */
    void clearHistory();

    // ==================== RECEIPT PARSING ====================

    /**
     * Extracts receipt data from raw OCR text.
     *
     * @param rawText OCR text from image
     * @param callback Response callback with JSON result
     */
    void parseReceipt(@NonNull String rawText, @NonNull AICallback callback);

    /**
     * Releases resources (for cleanup).
     */
    default void dispose() {
        clearHistory();
    }
}
