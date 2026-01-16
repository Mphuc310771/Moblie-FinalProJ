package com.smartbudget.app.data.remote;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface GroqService {
    
    @POST("openai/v1/chat/completions")
    Call<GroqModels.Response> chatCompletions(
            @Header("Authorization") String authHeader,
            @Body GroqModels.Request request
    );
}
