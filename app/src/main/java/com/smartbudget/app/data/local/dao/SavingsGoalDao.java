package com.smartbudget.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.smartbudget.app.data.local.entity.SavingsGoalEntity;

import java.util.List;

@Dao
public interface SavingsGoalDao {

    @Insert
    long insert(SavingsGoalEntity goal);

    @Update
    void update(SavingsGoalEntity goal);

    @Delete
    void delete(SavingsGoalEntity goal);

    @Query("SELECT * FROM savings_goals ORDER BY isCompleted ASC, deadline ASC")
    LiveData<List<SavingsGoalEntity>> getAllGoals();

    @Query("SELECT * FROM savings_goals WHERE isCompleted = 0 ORDER BY deadline ASC")
    LiveData<List<SavingsGoalEntity>> getActiveGoals();

    @Query("SELECT * FROM savings_goals WHERE isCompleted = 1 ORDER BY deadline DESC")
    LiveData<List<SavingsGoalEntity>> getCompletedGoals();

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    SavingsGoalEntity getGoalById(long id);

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    LiveData<SavingsGoalEntity> getGoalByIdLive(long id);

    @Query("UPDATE savings_goals SET currentAmount = currentAmount + :amount WHERE id = :goalId")
    void addSavings(long goalId, double amount);

    @Query("UPDATE savings_goals SET isCompleted = 1 WHERE id = :goalId")
    void markAsCompleted(long goalId);

    @Query("SELECT SUM(currentAmount) FROM savings_goals WHERE isCompleted = 0")
    LiveData<Double> getTotalSavings();

    @Query("SELECT COUNT(*) FROM savings_goals WHERE isCompleted = 0")
    LiveData<Integer> getActiveGoalCount();

    // Sync methods for fragment
    @Query("SELECT * FROM savings_goals WHERE isCompleted = 0 ORDER BY deadline ASC")
    List<SavingsGoalEntity> getActiveGoalsSync();

    @Query("SELECT * FROM savings_goals WHERE isCompleted = 1 ORDER BY deadline DESC")
    List<SavingsGoalEntity> getCompletedGoalsSync();

    @Query("SELECT SUM(currentAmount) FROM savings_goals")
    Double getTotalSavedSync();

    @Query("UPDATE savings_goals SET currentAmount = :amount, isCompleted = :isCompleted WHERE id = :goalId")
    void updateProgress(long goalId, double amount, boolean isCompleted);

    @Query("DELETE FROM savings_goals")
    void deleteAll();
}
