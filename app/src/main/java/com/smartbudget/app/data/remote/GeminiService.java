package com.smartbudget.app.data.remote;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GeminiService {
    
    @POST("{apiVersion}/models/{model}:generateContent")
    Call<GeminiModels.Response> generateContent(
            @Path("apiVersion") String apiVersion,
            @Path("model") String model,
            @Query("key") String apiKey,
            @Body GeminiModels.Request request
    );

    @GET("{apiVersion}/models")
    Call<GeminiModels.ModelList> listModels(
            @Path("apiVersion") String apiVersion,
            @Query("key") String apiKey
    );
}
