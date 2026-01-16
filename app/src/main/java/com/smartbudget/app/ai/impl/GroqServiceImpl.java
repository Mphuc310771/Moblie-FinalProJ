package com.smartbudget.app.ai.impl;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.smartbudget.app.ai.AICallback;
import com.smartbudget.app.ai.AIConfig;
import com.smartbudget.app.ai.AIService;
import com.smartbudget.app.ai.Result;
import com.smartbudget.app.data.remote.GroqModels;
import com.smartbudget.app.data.remote.GroqService;

import java.io.IOException;
import java.util.ArrayList;
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
 * Groq AI service implementation using Strategy Pattern.
 * Supports Llama 3.3, Mixtral, and Gemma models.
 * 
 * <h2>Features:</h2>
 * <ul>
 *   <li>Exponential backoff retry on failures</li>
 *   <li>Conversation history management</li>
 *   <li>Rate limit handling</li>
 * </ul>
 * 
 * @author SmartBudget Development Team
 * @version 1.0
 */
public class GroqServiceImpl implements AIService {

    private static final String TAG = "GroqServiceImpl";

    // ==================== CONFIGURATION ====================
    
    private final AIConfig config;
    private final GroqService groqService;
    private final Handler mainHandler;
    private final List<GroqModels.Message> chatHistory;
    
    private String currentModel;

    // ==================== SYSTEM PROMPT ====================
    
    private static final String SYSTEM_PROMPT = 
        "Bạn là chuyên gia tư vấn tài chính cá nhân thông minh của SmartBudget.\n" +
        "Nhiệm vụ: Giúp người dùng quản lý chi tiêu, tiết kiệm tiền và đạt mục tiêu tài chính.\n" +
        "Phong cách: Thân thiện, chuyên nghiệp, đưa ra lời khuyên cụ thể và thực tế.\n" +
        "Ngôn ngữ: Tiếng Việt, dễ hiểu, tránh thuật ngữ phức tạp.\n" +
        "Định dạng: Sử dụng bullet points, emoji khi phù hợp.";

    // ==================== CONSTRUCTOR ====================

    /**
     * Creates a new Groq service with default configuration.
     */
    public GroqServiceImpl() {
        this(AIConfig.getInstance());
    }

    /**
     * Creates a new Groq service with custom configuration.
     *
     * @param config AI configuration
     */
    public GroqServiceImpl(@NonNull AIConfig config) {
        this.config = config;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.chatHistory = new ArrayList<>();
        this.currentModel = AIConfig.DEFAULT_GROQ_MODEL;
        
        this.groqService = createRetrofit().create(GroqService.class);
        
        // Add system message
        chatHistory.add(new GroqModels.Message("system", SYSTEM_PROMPT));
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
                .baseUrl(AIConfig.GROQ_BASE_URL)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // ==================== AIService IMPLEMENTATION ====================

    @NonNull
    @Override
    public String getProviderName() {
        return "Groq";
    }

    @NonNull
    @Override
    public String getCurrentModel() {
        return currentModel;
    }

    @Override
    public boolean isConfigured() {
        return config.isGroqConfigured();
    }

    @Override
    public void chat(@NonNull String message, @NonNull AICallback callback) {
        if (!isConfigured()) {
            callback.onError("Groq API key chưa được cấu hình", Result.ERROR_AUTH);
            return;
        }

        if (message.trim().isEmpty()) {
            callback.onError("Tin nhắn không được để trống", Result.ERROR_INVALID_REQUEST);
            return;
        }

        // Add user message to history
        chatHistory.add(new GroqModels.Message("user", message));
        
        // Execute with retry
        executeWithRetry(callback, 0);
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
        chatHistory.add(new GroqModels.Message("system", SYSTEM_PROMPT));
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
        
        // Use a temporary request to avoid polluting chat history
        List<GroqModels.Message> messages = new ArrayList<>();
        messages.add(new GroqModels.Message("system", "You are a receipt parsing API. Output strict JSON only."));
        messages.add(new GroqModels.Message("user", prompt));

        GroqModels.Request request = new GroqModels.Request(currentModel, messages);
        request.temperature = 0.1; // Low temperature for consistent formatting

        String apiKey = config.getGroqApiKey();
        if (apiKey == null) {
            callback.onError("API key missing", Result.ERROR_AUTH);
            return;
        }

        groqService.chatCompletions("Bearer " + apiKey, request).enqueue(new Callback<GroqModels.Response>() {
            @Override
            public void onResponse(@NonNull Call<GroqModels.Response> call, @NonNull Response<GroqModels.Response> response) {
                 try {
                    if (response.isSuccessful() && response.body() != null &&
                        !response.body().choices.isEmpty()) {
                        String json = response.body().choices.get(0).message.content;
                        // Clean markdown if present
                        json = json.replace("```json", "").replace("```", "").trim();
                        final String finalJson = json;
                        mainHandler.post(() -> callback.onSuccess(finalJson));
                    } else {
                        mainHandler.post(() -> callback.onError("Empty response", Result.ERROR_SERVER));
                    }
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError(e.getMessage(), Result.ERROR_UNKNOWN));
                }
            }

            @Override
            public void onFailure(@NonNull Call<GroqModels.Response> call, @NonNull Throwable t) {
                mainHandler.post(() -> callback.onError(t.getMessage(), Result.ERROR_NETWORK));
            }
        });
    }

    // ==================== RETRY LOGIC ====================

    /**
     * Executes API call with exponential backoff retry.
     *
     * @param callback Response callback
     * @param attempt Current attempt number (0-based)
     */
    private void executeWithRetry(@NonNull AICallback callback, int attempt) {
        String apiKey = config.getGroqApiKey();
        if (apiKey == null) {
            callback.onError("API key không hợp lệ", Result.ERROR_AUTH);
            return;
        }

        GroqModels.Request request = new GroqModels.Request(
            currentModel,
            new ArrayList<>(chatHistory)
        );

        groqService.chatCompletions("Bearer " + apiKey, request).enqueue(new Callback<GroqModels.Response>() {
            @Override
            public void onResponse(@NonNull Call<GroqModels.Response> call, 
                                   @NonNull Response<GroqModels.Response> response) {
                handleResponse(response, callback, attempt);
            }

            @Override
            public void onFailure(@NonNull Call<GroqModels.Response> call, @NonNull Throwable t) {
                handleNetworkError(t, callback, attempt);
            }
        });
    }

    /**
     * Handles API response with retry logic for errors.
     */
    private void handleResponse(@NonNull Response<GroqModels.Response> response,
                                @NonNull AICallback callback, int attempt) {
        try {
            if (response.isSuccessful() && response.body() != null &&
                response.body().choices != null && !response.body().choices.isEmpty()) {
                
                String content = response.body().choices.get(0).message.content;
                if (content != null && !content.isEmpty()) {
                    // Save to history
                    chatHistory.add(new GroqModels.Message("assistant", content));
                    mainHandler.post(() -> callback.onSuccess(content));
                    return;
                }
            }

            int code = response.code();
            Log.w(TAG, "API Error: " + code);

            // Retry on retryable errors
            if (config.shouldRetry(code) && attempt < AIConfig.MAX_RETRY_ATTEMPTS) {
                long delay = code == 429 ? AIConfig.RATE_LIMIT_DELAY_MS : config.getRetryDelay(attempt);
                Log.d(TAG, "Retrying in " + delay + "ms (attempt " + (attempt + 1) + ")");
                mainHandler.postDelayed(() -> executeWithRetry(callback, attempt + 1), delay);
            } else {
                int errorCode = code == 429 ? Result.ERROR_RATE_LIMIT : Result.ERROR_SERVER;
                mainHandler.post(() -> callback.onError("Lỗi API: " + code, errorCode));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling response", e);
            mainHandler.post(() -> callback.onError("Lỗi xử lý: " + e.getMessage(), Result.ERROR_UNKNOWN));
        }
    }

    /**
     * Handles network errors with retry logic.
     */
    private void handleNetworkError(@NonNull Throwable t, @NonNull AICallback callback, int attempt) {
        Log.e(TAG, "Network error", t);

        boolean isRetryable = t instanceof IOException;
        
        if (isRetryable && attempt < AIConfig.MAX_RETRY_ATTEMPTS) {
            long delay = config.getRetryDelay(attempt);
            Log.d(TAG, "Network retry in " + delay + "ms");
            mainHandler.postDelayed(() -> executeWithRetry(callback, attempt + 1), delay);
        } else {
            mainHandler.post(() -> callback.onError("Lỗi mạng: " + t.getMessage(), Result.ERROR_NETWORK));
        }
    }

    /**
     * Sets the model to use.
     *
     * @param model Model identifier
     */
    public void setModel(@NonNull String model) {
        this.currentModel = model;
    }
}
