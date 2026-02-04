package com.smartbudget.app.presentation.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.smartbudget.app.data.local.dao.ExpenseDao;
import com.smartbudget.app.data.local.entity.CategoryEntity;
import com.smartbudget.app.data.local.entity.ExpenseEntity;
import com.smartbudget.app.data.repository.CategoryRepository;
import com.smartbudget.app.data.repository.ExpenseRepository;
import com.smartbudget.app.utils.DateUtils;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    private final LiveData<List<ExpenseEntity>> recentExpenses;
    private final LiveData<List<CategoryEntity>> categories;
    private final LiveData<Double> monthlyTotal;
    private final LiveData<List<ExpenseDao.CategoryTotal>> categoryTotals;
    private final LiveData<Double> previousMonthTotal;
    private final MediatorLiveData<String> spendingInsight = new MediatorLiveData<>();

    // Income, Expense and Balance for Dashboard
    private final LiveData<Double> monthlyIncome;
    private final LiveData<Double> monthlyExpense;
    private final MediatorLiveData<Double> balance = new MediatorLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);

        expenseRepository = new ExpenseRepository(application);
        categoryRepository = new CategoryRepository(application);

        // Get current month range
        int month = DateUtils.getCurrentMonth();
        int year = DateUtils.getCurrentYear();
        long startOfMonth = DateUtils.getStartOfMonth(month, year);
        long endOfMonth = DateUtils.getEndOfMonth(month, year);

        // Get previous month range
        int prevMonth = month - 1;
        int prevYear = year;
        if (prevMonth == 0) {
            prevMonth = 12;
            prevYear--;
        }
        long startOfPrevMonth = DateUtils.getStartOfMonth(prevMonth, prevYear);
        long endOfPrevMonth = DateUtils.getEndOfMonth(prevMonth, prevYear);

        // Initialize LiveData
        recentExpenses = expenseRepository.getRecentExpenses(10);
        categories = categoryRepository.getAllCategories();
        monthlyTotal = expenseRepository.getTotalExpenseByDateRange(startOfMonth, endOfMonth);
        previousMonthTotal = expenseRepository.getTotalExpenseByDateRange(startOfPrevMonth, endOfPrevMonth);
        
        categoryTotals = expenseRepository.getExpenseTotalsByCategory(startOfMonth, endOfMonth);
        
        // Income and Expense for Dashboard balance card
        monthlyIncome = expenseRepository.getTotalIncomeByDateRange(startOfMonth, endOfMonth);
        monthlyExpense = expenseRepository.getTotalExpenseByDateRange(startOfMonth, endOfMonth);
        
        // Calculate balance = income - expense
        balance.addSource(monthlyIncome, income -> updateBalance());
        balance.addSource(monthlyExpense, expense -> updateBalance());
        
        setupInsight();
    }
    
    private void updateBalance() {
        Double income = monthlyIncome.getValue();
        Double expense = monthlyExpense.getValue();
        if (income == null) income = 0.0;
        if (expense == null) expense = 0.0;
        balance.setValue(income - expense);
    }

    public LiveData<List<ExpenseEntity>> getRecentExpenses() {
        return recentExpenses;
    }

    public LiveData<List<CategoryEntity>> getCategories() {
        return categories;
    }

    public LiveData<Double> getMonthlyTotal() {
        return monthlyTotal;
    }

    public LiveData<List<ExpenseDao.CategoryTotal>> getCategoryTotals() {
        return categoryTotals;
    }

    public LiveData<Double> getTotalIncome() {
        return monthlyIncome;
    }

    public LiveData<Double> getTotalExpense() {
        return monthlyExpense;
    }

    public LiveData<Double> getBalance() {
        return balance;
    }

    private void setupInsight() {
        spendingInsight.addSource(monthlyTotal, current -> updateInsight());
        spendingInsight.addSource(previousMonthTotal, prev -> updateInsight());
    }

    private void updateInsight() {
        Double current = monthlyTotal.getValue();
        Double previous = previousMonthTotal.getValue();

        if (current == null) current = 0.0;
        if (previous == null) previous = 0.0;

        if (previous == 0) {
            spendingInsight.setValue(getApplication().getString(com.smartbudget.app.R.string.insight_start_spending));
            return;
        }

        double diff = current - previous;
        double percent = (Math.abs(diff) / previous) * 100;
        String formattedPercent = String.format("%.0f%%", percent);

        if (diff < 0) {
            spendingInsight.setValue(getApplication().getString(com.smartbudget.app.R.string.insight_lower_spending, formattedPercent));
        } else if (diff > 0) {
            spendingInsight.setValue(getApplication().getString(com.smartbudget.app.R.string.insight_higher_spending, formattedPercent));
        } else {
            spendingInsight.setValue(getApplication().getString(com.smartbudget.app.R.string.insight_equal_spending));
        }
    }

    public LiveData<String> getSpendingInsight() {
        return spendingInsight;
    }

    public void deleteExpense(ExpenseEntity expense) {
        expenseRepository.delete(expense);
    }

    public void insertExpense(ExpenseEntity expense) {
        expenseRepository.insert(expense);
    }
}
