package com.smartbudget.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.smartbudget.app.data.local.entity.ExpenseEntity;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    long insert(ExpenseEntity expense);

    @Update
    void update(ExpenseEntity expense);

    @Delete
    void delete(ExpenseEntity expense);

    @Query("DELETE FROM expenses WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM expenses ORDER BY date DESC, createdAt DESC")
    LiveData<List<ExpenseEntity>> getAllExpenses();

    @Query("SELECT * FROM expenses WHERE id = :id")
    ExpenseEntity getExpenseById(long id);

    @Query("SELECT * FROM expenses WHERE id = :id")
    LiveData<ExpenseEntity> getExpenseByIdLive(long id);

    @Query("SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> getExpensesByDateRange(long startDate, long endDate);

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId ORDER BY date DESC")
    LiveData<List<ExpenseEntity>> getExpensesByCategory(long categoryId);

    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT :limit")
    LiveData<List<ExpenseEntity>> getRecentExpenses(int limit);

    // Aggregation queries
    @Query("SELECT SUM(amount) FROM expenses WHERE date >= :startDate AND date <= :endDate")
    LiveData<Double> getTotalExpenseByDateRange(long startDate, long endDate);

    @Query("SELECT SUM(amount) FROM expenses WHERE categoryId = :categoryId AND date >= :startDate AND date <= :endDate")
    double getTotalByCategoryAndDateRange(long categoryId, long startDate, long endDate);

    @Query("SELECT SUM(amount) FROM expenses WHERE date >= :startDate AND date <= :endDate")
    double getTotalByDateRange(long startDate, long endDate);

    // For chart data - group by category
    @Query("SELECT categoryId, SUM(amount) as total FROM expenses WHERE date >= :startDate AND date <= :endDate GROUP BY categoryId")
    LiveData<List<CategoryTotal>> getExpenseTotalsByCategory(long startDate, long endDate);

    @Query("SELECT * FROM expenses ORDER BY date DESC, createdAt DESC")
    List<ExpenseEntity> getAllExpensesSync();

    // For sync
    @Query("SELECT * FROM expenses WHERE isSynced = 0")
    List<ExpenseEntity> getUnsyncedExpenses();

    @Query("UPDATE expenses SET isSynced = 1 WHERE id IN (:ids)")
    void markAsSynced(List<Long> ids);

    @Query("DELETE FROM expenses")
    void deleteAll();

    // Helper class for aggregation
    class CategoryTotal {
        public Long categoryId;
        public double total;
    }
}
