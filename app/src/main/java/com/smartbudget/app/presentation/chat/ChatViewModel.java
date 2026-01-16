package com.smartbudget.app.presentation.chat;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartbudget.app.data.local.entity.ChatMessageEntity;
import com.smartbudget.app.data.repository.ChatRepository;

import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository repository;
    private final LiveData<List<ChatMessageEntity>> messages;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ChatViewModel(@NonNull Application application) {
        super(application);
        repository = new ChatRepository(application);
        messages = repository.getAllMessages();
    }

    public LiveData<List<ChatMessageEntity>> getMessages() {
        return messages;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void sendMessage(String message) {
        if (message == null || message.trim().isEmpty()) return;

        isLoading.setValue(true);
        repository.sendMessage(message, new ChatRepository.Callback() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void clearHistory() {
        repository.clearHistory();
    }
}
