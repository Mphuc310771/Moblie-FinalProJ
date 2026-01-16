package com.smartbudget.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.smartbudget.app.data.local.entity.BudgetEntity;

import java.util.List;

@Dao
public interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BudgetEntity budget);

    @Update
    void update(BudgetEntity budget);

    @Delete
    void delete(BudgetEntity budget);

    @Query("SELECT b.*, " +
           "(SELECT COALESCE(SUM(amount), 0) FROM expenses e " +
           "WHERE e.categoryId = b.categoryId " +
           "AND strftime('%m', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%02d', :month) " +
           "AND strftime('%Y', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%d', :year)) as spentAmount " +
           "FROM budgets b WHERE b.categoryId IS NOT NULL AND b.month = :month AND b.year = :year ORDER BY b.categoryId")
    LiveData<List<BudgetEntity>> getBudgetsByMonthYear(int month, int year);

    @Query("SELECT b.id, b.categoryId, b.limitAmount, b.month, b.year, " +
           "(SELECT COALESCE(SUM(amount), 0) FROM expenses e " +
           "WHERE strftime('%m', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%02d', :month) " +
           "AND strftime('%Y', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%d', :year)) as spentAmount " +
           "FROM budgets b WHERE b.categoryId IS NULL AND b.month = :month AND b.year = :year")
    BudgetEntity getTotalBudget(int month, int year);

    @Query("SELECT b.id, b.categoryId, b.limitAmount, b.month, b.year, " +
           "(SELECT COALESCE(SUM(amount), 0) FROM expenses e " +
           "WHERE strftime('%m', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%02d', :month) " +
           "AND strftime('%Y', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%d', :year)) as spentAmount " +
           "FROM budgets b WHERE b.categoryId IS NULL AND b.month = :month AND b.year = :year")
    LiveData<BudgetEntity> getTotalBudgetLive(int month, int year);

    @Query("SELECT b.id, b.categoryId, b.limitAmount, b.month, b.year, " +
           "(SELECT COALESCE(SUM(amount), 0) FROM expenses e " +
           "WHERE e.categoryId = :categoryId " +
           "AND strftime('%m', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%02d', :month) " +
           "AND strftime('%Y', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%d', :year)) as spentAmount " +
           "FROM budgets b WHERE b.categoryId = :categoryId AND b.month = :month AND b.year = :year")
    BudgetEntity getBudgetByCategory(long categoryId, int month, int year);

    @Query("SELECT b.id, b.categoryId, b.limitAmount, b.month, b.year, " +
           "(SELECT COALESCE(SUM(amount), 0) FROM expenses e " +
           "WHERE e.categoryId = :categoryId " +
           "AND strftime('%m', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%02d', :month) " +
           "AND strftime('%Y', datetime(e.date/1000, 'unixepoch', 'localtime')) = printf('%d', :year)) as spentAmount " +
           "FROM budgets b WHERE b.categoryId = :categoryId AND b.month = :month AND b.year = :year")
    LiveData<BudgetEntity> getBudgetByCategoryLive(long categoryId, int month, int year);

    @Query("UPDATE budgets SET spentAmount = :amount WHERE id = :budgetId")
    void updateSpentAmount(long budgetId, double amount);

    @Query("DELETE FROM budgets WHERE month = :month AND year = :year")
    void deleteBudgetsByMonthYear(int month, int year);

    @Query("SELECT * FROM budgets WHERE id = :id")
    BudgetEntity getBudgetById(long id);

    @Query("SELECT * FROM budgets ORDER BY month DESC, year DESC")
    List<BudgetEntity> getAllBudgetsSync();

    @Query("DELETE FROM budgets")
    void deleteAll();
}
