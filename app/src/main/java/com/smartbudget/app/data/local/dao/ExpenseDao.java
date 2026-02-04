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

    // Aggregation queries - only count EXPENSE categories (type=0), not income
    @Query("SELECT SUM(e.amount) FROM expenses e " +
           "INNER JOIN categories c ON e.categoryId = c.id " +
           "WHERE c.type = 0 AND e.date >= :startDate AND e.date <= :endDate")
    LiveData<Double> getTotalExpenseByDateRange(long startDate, long endDate);

    // Total INCOME (category type=1)
    @Query("SELECT SUM(e.amount) FROM expenses e " +
           "INNER JOIN categories c ON e.categoryId = c.id " +
           "WHERE c.type = 1 AND e.date >= :startDate AND e.date <= :endDate")
    LiveData<Double> getTotalIncomeByDateRange(long startDate, long endDate);

    @Query("SELECT SUM(amount) FROM expenses WHERE categoryId = :categoryId AND date >= :startDate AND date <= :endDate")
    double getTotalByCategoryAndDateRange(long categoryId, long startDate, long endDate);

    @Query("SELECT SUM(e.amount) FROM expenses e " +
           "INNER JOIN categories c ON e.categoryId = c.id " +
           "WHERE c.type = 0 AND e.date >= :startDate AND e.date <= :endDate")
    double getTotalByDateRange(long startDate, long endDate);

    // Sync version of income query (for background thread)
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM expenses e " +
           "INNER JOIN categories c ON e.categoryId = c.id " +
           "WHERE c.type = 1 AND e.date >= :startDate AND e.date <= :endDate")
    double getTotalIncomeSync(long startDate, long endDate);

    // For chart data - group by EXPENSE categories only
    @Query("SELECT e.categoryId, SUM(e.amount) as total FROM expenses e " +
           "INNER JOIN categories c ON e.categoryId = c.id " +
           "WHERE c.type = 0 AND e.date >= :startDate AND e.date <= :endDate GROUP BY e.categoryId")
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
