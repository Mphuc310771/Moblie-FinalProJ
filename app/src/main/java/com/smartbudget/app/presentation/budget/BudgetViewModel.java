package com.smartbudget.app.presentation.budget;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartbudget.app.data.local.entity.BudgetEntity;
import com.smartbudget.app.data.local.entity.CategoryEntity;
import com.smartbudget.app.data.repository.BudgetRepository;
import com.smartbudget.app.data.repository.CategoryRepository;
import com.smartbudget.app.utils.DateUtils;

import java.util.List;

public class BudgetViewModel extends AndroidViewModel {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    private final int currentMonth;
    private final int currentYear;

    public BudgetViewModel(@NonNull Application application) {
        super(application);
        budgetRepository = new BudgetRepository(application);
        categoryRepository = new CategoryRepository(application);

        currentMonth = DateUtils.getCurrentMonth();
        currentYear = DateUtils.getCurrentYear();
    }

    public LiveData<List<CategoryEntity>> getExpenseCategories() {
        return categoryRepository.getExpenseCategories();
    }

    public LiveData<List<BudgetEntity>> getCurrentMonthBudgets() {
        return budgetRepository.getBudgetsByMonthYear(currentMonth, currentYear);
    }

    public LiveData<BudgetEntity> getTotalBudget() {
        return budgetRepository.getTotalBudget(currentMonth, currentYear);
    }

    public void saveTotalBudget(double amount) {
        BudgetEntity budget = new BudgetEntity(null, amount, currentMonth, currentYear);
        budgetRepository.insertOrUpdate(budget);
    }

    public void saveCategoryBudget(long categoryId, double amount) {
        BudgetEntity budget = new BudgetEntity(categoryId, amount, currentMonth, currentYear);
        budgetRepository.insertOrUpdate(budget);
    }

    public void deleteBudget(BudgetEntity budget) {
        budgetRepository.delete(budget);
    }

    public int getCurrentMonth() {
        return currentMonth;
    }

    public int getCurrentYear() {
        return currentYear;
    }
}
