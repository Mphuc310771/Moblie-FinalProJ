package com.smartbudget.app.ai.impl;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.smartbudget.app.ai.AICallback;
import com.smartbudget.app.ai.AIConfig;
import com.smartbudget.app.ai.AIService;
import com.smartbudget.app.ai.Result;
import com.smartbudget.app.data.remote.GeminiModels;
import com.smartbudget.app.data.remote.GeminiService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Google Gemini AI service implementation using Strategy Pattern.
 * Supports multiple Gemini models with automatic fallback.
 * 
 * <h2>Features:</h2>
 * <ul>
 *   <li>Multi-model fallback (2.0-flash → 1.5-flash → pro)</li>
 *   <li>Exponential backoff retry on failures</li>
 *   <li>Conversation history management</li>
 * </ul>
 * 
 * @author SmartBudget Development Team
 * @version 1.0
 */
public class GeminiServiceImpl implements AIService {

    private static final String TAG = "GeminiServiceImpl";

    // ==================== CONFIGURATION ====================
    
    private final AIConfig config;
    private final Retrofit retrofit;
    private final Handler mainHandler;
    private final List<GeminiModels.Content> chatHistory;
    
    private String currentModel;
    private int currentModelIndex = 0;

    // ==================== SYSTEM PROMPT ====================
    
    private static final String SYSTEM_PROMPT = 
        "Bạn là chuyên gia tư vấn tài chính cá nhân thông minh của SmartBudget.\n" +
        "Nhiệm vụ: Giúp người dùng quản lý chi tiêu, tiết kiệm tiền và đạt mục tiêu tài chính.\n" +
        "Phong cách: Thân thiện, chuyên nghiệp, đưa ra lời khuyên cụ thể và thực tế.\n" +
        "Ngôn ngữ: Tiếng Việt, dễ hiểu, tránh thuật ngữ phức tạp.\n" +
        "Định dạng: Sử dụng bullet points, emoji khi phù hợp.";

    // ==================== CONSTRUCTOR ====================

    /**
     * Creates a new Gemini service with default configuration.
     */
    public GeminiServiceImpl() {
        this(AIConfig.getInstance());
    }

    /**
     * Creates a new Gemini service with custom configuration.
     *
     * @param config AI configuration
     */
    public GeminiServiceImpl(@NonNull AIConfig config) {
        this.config = config;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.chatHistory = new ArrayList<>();
        this.currentModel = AIConfig.DEFAULT_GEMINI_MODEL;
        
        this.retrofit = createRetrofit();
        
        // Add system message as first user message
        addToHistory("user", SYSTEM_PROMPT);
        addToHistory("model", "Tôi đã sẵn sàng hỗ trợ bạn về tài chính cá nhân!");
    }

    // ==================== RETROFIT SETUP ====================

    private Retrofit createRetrofit() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(AIConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(AIConfig.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(AIConfig.WRITE_TIMEOUT, TimeUnit.SECONDS);

        // Add logging in debug mode
        if (config.isDebugMode()) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(logging);
        }

        return new Retrofit.Builder()
                .baseUrl(AIConfig.GEMINI_BASE_URL)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // ==================== AIService IMPLEMENTATION ====================

    @NonNull
    @Override
    public String getProviderName() {
        return "Gemini";
    }

    @NonNull
    @Override
    public String getCurrentModel() {
        return currentModel;
    }

    @Override
    public boolean isConfigured() {
        return config.isGeminiConfigured();
    }

    @Override
    public void chat(@NonNull String message, @NonNull AICallback callback) {
        if (!isConfigured()) {
            callback.onError("Gemini API key chưa được cấu hình", Result.ERROR_AUTH);
            return;
        }

        if (message.trim().isEmpty()) {
            callback.onError("Tin nhắn không được để trống", Result.ERROR_INVALID_REQUEST);
            return;
        }

        // Add user message to history
        addToHistory("user", message);
        
        // Reset model index for new conversation turn
        currentModelIndex = 0;
        
        // Execute with current model
        executeRequest(callback, 0);
    }

    @Override
    public void getFinancialAdvice(@NonNull String query, @NonNull AICallback callback) {
        chat(query, callback);
    }

    @Override
    public void analyzeSpending(@NonNull String spendingData, @NonNull AICallback callback) {
        String prompt = "Phân tích chi tiêu sau và đưa ra nhận xét, lời khuyên:\n\n" + spendingData;
        chat(prompt, callback);
    }

    @Override
    public void suggestBudget(double monthlyIncome, @NonNull AICallback callback) {
        String prompt = String.format(
            "Thu nhập hàng tháng của tôi là %,.0f đồng. " +
            "Hãy đề xuất cách phân bổ ngân sách hợp lý cho các khoản chi tiêu.", 
            monthlyIncome
        );
        chat(prompt, callback);
    }

    @Override
    public void clearHistory() {
        chatHistory.clear();
        addToHistory("user", SYSTEM_PROMPT);
        addToHistory("model", "Tôi đã sẵn sàng hỗ trợ bạn về tài chính cá nhân!");
    }

    @Override
    public void parseReceipt(@NonNull String rawText, @NonNull AICallback callback) {
        String prompt = "Extract data from this receipt text into valid JSON format.\n" +
                "Fields required: \n" +
                "- amount (number, total paid)\n" +
                "- merchant (string, store name)\n" +
                "- date (string, DD/MM/YYYY format)\n" +
                "- items (list of strings, extracted line items or description)\n\n" +
                "Raw Text:\n" + rawText + "\n\n" +
                "Return ONLY raw JSON, no markdown, no explanation.";

        chat(prompt, new AICallback() {
            @Override
            public void onSuccess(String response) {
                // Clean markdown if present
                String json = response.replace("```json", "").replace("```", "").trim();
                callback.onSuccess(json);
            }

            @Override
            public void onError(String error, int code) {
                callback.onError(error, code);
            }
        });
    }

    // ==================== REQUEST EXECUTION ====================

    /**
     * Executes API request with model fallback and retry.
     *
     * @param callback Response callback
     * @param attempt Current retry attempt
     */
    private void executeRequest(@NonNull AICallback callback, int attempt) {
        String apiKey = config.getGeminiApiKey();
        if (apiKey == null) {
            callback.onError("API key không hợp lệ", Result.ERROR_AUTH);
            return;
        }

        // Get current model
        String model = getModelForAttempt();
        String apiVersion = model.equals("gemini-pro") ? "v1" : "v1beta";
        
        GeminiService service = retrofit.create(GeminiService.class);
        GeminiModels.Request request = new GeminiModels.Request(new ArrayList<>(chatHistory));

        service.generateContent(apiVersion, model, apiKey, request)
                .enqueue(new Callback<GeminiModels.Response>() {
                    @Override
                    public void onResponse(@NonNull Call<GeminiModels.Response> call,
                                           @NonNull Response<GeminiModels.Response> response) {
                        handleResponse(response, callback, attempt);
                    }

                    @Override
                    public void onFailure(@NonNull Call<GeminiModels.Response> call, @NonNull Throwable t) {
                        handleNetworkError(t, callback, attempt);
                    }
                });
    }

    /**
     * Gets model for current attempt (implements fallback).
     */
    private String getModelForAttempt() {
        if (currentModelIndex == 0) {
            return currentModel;
        }
        int fallbackIndex = currentModelIndex - 1;
        if (fallbackIndex < AIConfig.GEMINI_FALLBACK_MODELS.length) {
            return AIConfig.GEMINI_FALLBACK_MODELS[fallbackIndex];
        }
        return AIConfig.GEMINI_FALLBACK_MODELS[AIConfig.GEMINI_FALLBACK_MODELS.length - 1];
    }

    /**
     * Handles API response.
     */
    private void handleResponse(@NonNull Response<GeminiModels.Response> response,
                                @NonNull AICallback callback, int attempt) {
        try {
            if (response.isSuccessful() && response.body() != null &&
                response.body().candidates != null && !response.body().candidates.isEmpty()) {
                
                GeminiModels.Candidate candidate = response.body().candidates.get(0);
                if (candidate.content != null && candidate.content.parts != null &&
                    !candidate.content.parts.isEmpty()) {
                    
                    String content = candidate.content.parts.get(0).text;
                    if (content != null && !content.isEmpty()) {
                        addToHistory("model", content);
                        mainHandler.post(() -> callback.onSuccess(content));
                        return;
                    }
                }
            }

            int code = response.code();
            Log.w(TAG, "API Error: " + code + " (model: " + getModelForAttempt() + ")");

            // Try next model on 404, 400, or 503
            if ((code == 404 || code == 400 || code == 503) && 
                currentModelIndex < AIConfig.GEMINI_FALLBACK_MODELS.length) {
                currentModelIndex++;
                Log.d(TAG, "Trying fallback model: " + getModelForAttempt());
                mainHandler.post(() -> executeRequest(callback, 0));
                return;
            }

            // Retry on rate limit
            if (code == 429 && attempt < AIConfig.MAX_RETRY_ATTEMPTS) {
                long delay = AIConfig.RATE_LIMIT_DELAY_MS;
                Log.d(TAG, "Rate limited, retrying in " + delay + "ms");
                mainHandler.postDelayed(() -> executeRequest(callback, attempt + 1), delay);
                return;
            }

            int errorCode = code == 429 ? Result.ERROR_RATE_LIMIT : Result.ERROR_SERVER;
            mainHandler.post(() -> callback.onError("Lỗi API: " + code, errorCode));

        } catch (Exception e) {
            Log.e(TAG, "Error handling response", e);
            mainHandler.post(() -> callback.onError("Lỗi xử lý: " + e.getMessage(), Result.ERROR_UNKNOWN));
        }
    }

    /**
     * Handles network errors.
     */
    private void handleNetworkError(@NonNull Throwable t, @NonNull AICallback callback, int attempt) {
        Log.e(TAG, "Network error", t);

        if (t instanceof IOException && attempt < AIConfig.MAX_RETRY_ATTEMPTS) {
            long delay = config.getRetryDelay(attempt);
            Log.d(TAG, "Network retry in " + delay + "ms");
            mainHandler.postDelayed(() -> executeRequest(callback, attempt + 1), delay);
        } else {
            mainHandler.post(() -> callback.onError("Lỗi mạng: " + t.getMessage(), Result.ERROR_NETWORK));
        }
    }

    // ==================== HISTORY MANAGEMENT ====================

    private void addToHistory(@NonNull String role, @NonNull String text) {
        GeminiModels.Part part = new GeminiModels.Part(text);
        GeminiModels.Content content = new GeminiModels.Content(role, Collections.singletonList(part));
        chatHistory.add(content);
        
        // Limit history size
        while (chatHistory.size() > 20) {
            chatHistory.remove(2); // Keep system messages
        }
    }

    /**
     * Sets the primary model to use.
     *
     * @param model Model identifier
     */
    public void setModel(@NonNull String model) {
        this.currentModel = model;
        this.currentModelIndex = 0;
    }
}
