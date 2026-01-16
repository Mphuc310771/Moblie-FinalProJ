package com.smartbudget.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.smartbudget.app.data.local.AppDatabase;
import com.smartbudget.app.data.local.dao.BudgetDao;
import com.smartbudget.app.data.local.entity.BudgetEntity;

import java.util.List;

public class BudgetRepository {

    private final BudgetDao budgetDao;

    public BudgetRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        budgetDao = database.budgetDao();
    }

    // Insert or Update
    public void insertOrUpdate(BudgetEntity budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            budgetDao.insert(budget);
        });
    }

    // Delete
    public void delete(BudgetEntity budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            budgetDao.delete(budget);
        });
    }

    // Get budgets by month/year
    public LiveData<List<BudgetEntity>> getBudgetsByMonthYear(int month, int year) {
        return budgetDao.getBudgetsByMonthYear(month, year);
    }

    // Get total budget
    public LiveData<BudgetEntity> getTotalBudget(int month, int year) {
        return budgetDao.getTotalBudgetLive(month, year);
    }

    // Get budget by category
    public LiveData<BudgetEntity> getBudgetByCategory(long categoryId, int month, int year) {
        return budgetDao.getBudgetByCategoryLive(categoryId, month, year);
    }

    // Update spent amount
    public void updateSpentAmount(long budgetId, double amount) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            budgetDao.updateSpentAmount(budgetId, amount);
        });
    }
}
