package com.smartbudget.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.smartbudget.app.data.local.entity.ChatMessageEntity;

import java.util.List;

@Dao
public interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    androidx.lifecycle.LiveData<List<ChatMessageEntity>> getAllMessages();

    @Insert
    void insertMessage(ChatMessageEntity message);

    @Query("DELETE FROM chat_messages")
    void clearMessages();
}
