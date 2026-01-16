package com.smartbudget.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.smartbudget.app.data.local.entity.RecurringExpenseEntity;

import java.util.List;

@Dao
public interface RecurringExpenseDao {

    @Insert
    long insert(RecurringExpenseEntity expense);

    @Update
    void update(RecurringExpenseEntity expense);

    @Delete
    void delete(RecurringExpenseEntity expense);

    @Query("SELECT * FROM recurring_expenses WHERE isActive = 1 ORDER BY dayOfMonth ASC")
    LiveData<List<RecurringExpenseEntity>> getActiveRecurring();

    @Query("SELECT * FROM recurring_expenses ORDER BY createdAt DESC")
    LiveData<List<RecurringExpenseEntity>> getAllRecurring();

    @Query("SELECT * FROM recurring_expenses WHERE id = :id")
    RecurringExpenseEntity getById(long id);

    @Query("SELECT * FROM recurring_expenses WHERE nextDueDate <= :date AND isActive = 1")
    List<RecurringExpenseEntity> getDueExpenses(long date);

    @Query("UPDATE recurring_expenses SET nextDueDate = :nextDate WHERE id = :id")
    void updateNextDueDate(long id, long nextDate);

    @Query("UPDATE recurring_expenses SET isActive = :active WHERE id = :id")
    void setActive(long id, boolean active);

    @Query("SELECT SUM(amount) FROM recurring_expenses WHERE isActive = 1 AND frequency = 0")
    LiveData<Double> getMonthlyRecurringTotal();

    @Query("SELECT COUNT(*) FROM recurring_expenses WHERE isActive = 1")
    LiveData<Integer> getActiveCount();

    @Query("DELETE FROM recurring_expenses")
    void deleteAll();
}
