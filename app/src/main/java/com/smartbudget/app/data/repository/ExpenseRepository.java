package com.smartbudget.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.smartbudget.app.data.local.AppDatabase;
import com.smartbudget.app.data.local.dao.ExpenseDao;
import com.smartbudget.app.data.local.entity.ExpenseEntity;

import java.util.List;

public class ExpenseRepository {

    private final ExpenseDao expenseDao;

    public ExpenseRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        expenseDao = database.expenseDao();
    }

    // Insert
    public void insert(ExpenseEntity expense) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            expenseDao.insert(expense);
        });
    }

    public void insert(ExpenseEntity expense, OnExpenseInsertedListener listener) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = expenseDao.insert(expense);
            if (listener != null) {
                listener.onExpenseInserted(id);
            }
        });
    }

    // Update
    public void update(ExpenseEntity expense) {
        expense.setUpdatedAt(System.currentTimeMillis());
        expense.setSynced(false);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            expenseDao.update(expense);
        });
    }

    // Delete
    public void delete(ExpenseEntity expense) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            expenseDao.delete(expense);
        });
    }

    public void deleteById(long id) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            expenseDao.deleteById(id);
        });
    }

    // Get all
    public LiveData<List<ExpenseEntity>> getAllExpenses() {
        return expenseDao.getAllExpenses();
    }

    // Get by ID
    public LiveData<ExpenseEntity> getExpenseById(long id) {
        return expenseDao.getExpenseByIdLive(id);
    }

    // Get by date range
    public LiveData<List<ExpenseEntity>> getExpensesByDateRange(long startDate, long endDate) {
        return expenseDao.getExpensesByDateRange(startDate, endDate);
    }

    // Get by category
    public LiveData<List<ExpenseEntity>> getExpensesByCategory(long categoryId) {
        return expenseDao.getExpensesByCategory(categoryId);
    }

    // Get recent
    public LiveData<List<ExpenseEntity>> getRecentExpenses(int limit) {
        return expenseDao.getRecentExpenses(limit);
    }

    // Get total by date range
    public LiveData<Double> getTotalExpenseByDateRange(long startDate, long endDate) {
        return expenseDao.getTotalExpenseByDateRange(startDate, endDate);
    }

    // Get totals by category
    public LiveData<List<ExpenseDao.CategoryTotal>> getExpenseTotalsByCategory(long startDate, long endDate) {
        return expenseDao.getExpenseTotalsByCategory(startDate, endDate);
    }

    // Callback interface
    public interface OnExpenseInsertedListener {
        void onExpenseInserted(long id);
    }
}
