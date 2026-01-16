package com.smartbudget.app.presentation.reports;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.smartbudget.app.data.local.dao.ExpenseDao;
import com.smartbudget.app.data.local.entity.CategoryEntity;
import com.smartbudget.app.data.repository.CategoryRepository;
import com.smartbudget.app.data.repository.ExpenseRepository;
import com.smartbudget.app.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReportsViewModel extends AndroidViewModel {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    // 0 = weekly, 1 = monthly, 2 = yearly, 3 = custom
    private final MutableLiveData<Integer> selectedTimeRange = new MutableLiveData<>(1);

    private final MutableLiveData<Long> startDate = new MutableLiveData<>();
    private final MutableLiveData<Long> endDate = new MutableLiveData<>();

    // Reactive LiveData that updates when date range changes
    private final MediatorLiveData<Double> totalExpense = new MediatorLiveData<>();
    private final MediatorLiveData<List<ExpenseDao.CategoryTotal>> categoryTotals = new MediatorLiveData<>();
    
    private LiveData<Double> currentTotalSource;
    private LiveData<List<ExpenseDao.CategoryTotal>> currentCategorySource;

    public ReportsViewModel(@NonNull Application application) {
        super(application);
        expenseRepository = new ExpenseRepository(application);
        categoryRepository = new CategoryRepository(application);

        // Setup reactive data loading
        setupReactiveData();
        
        // Default to current month
        setMonthlyRange();
    }
    
    private void setupReactiveData() {
        // When startDate or endDate changes, reload data
        startDate.observeForever(date -> reloadData());
        endDate.observeForever(date -> reloadData());
    }
    
    private void reloadData() {
        Long start = startDate.getValue();
        Long end = endDate.getValue();
        
        if (start == null || end == null) return;
        
        // Remove old sources
        if (currentTotalSource != null) {
            totalExpense.removeSource(currentTotalSource);
        }
        if (currentCategorySource != null) {
            categoryTotals.removeSource(currentCategorySource);
        }
        
        // Add new sources
        currentTotalSource = expenseRepository.getTotalExpenseByDateRange(start, end);
        totalExpense.addSource(currentTotalSource, value -> {
            totalExpense.setValue(value != null ? value : 0.0);
        });
        
        currentCategorySource = expenseRepository.getExpenseTotalsByCategory(start, end);
        categoryTotals.addSource(currentCategorySource, value -> {
            categoryTotals.setValue(value != null ? value : new ArrayList<>());
        });
    }

    public LiveData<List<CategoryEntity>> getCategories() {
        return categoryRepository.getAllCategories();
    }

    public MutableLiveData<Integer> getSelectedTimeRange() {
        return selectedTimeRange;
    }

    public void setTimeRange(int range) {
        selectedTimeRange.setValue(range);
        switch (range) {
            case 0:
                setWeeklyRange();
                break;
            case 1:
                setMonthlyRange();
                break;
            case 2:
                setYearlyRange();
                break;
            // case 3 = custom, dont change dates
        }
    }

    private void setWeeklyRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        long start = DateUtils.getStartOfDay(cal.getTimeInMillis());

        cal.add(Calendar.DAY_OF_WEEK, 6);
        long end = DateUtils.getEndOfDay(cal.getTimeInMillis());
        
        startDate.setValue(start);
        endDate.setValue(end);
    }

    private void setMonthlyRange() {
        int month = DateUtils.getCurrentMonth();
        int year = DateUtils.getCurrentYear();
        startDate.setValue(DateUtils.getStartOfMonth(month, year));
        endDate.setValue(DateUtils.getEndOfMonth(month, year));
    }

    private void setYearlyRange() {
        int year = DateUtils.getCurrentYear();
        startDate.setValue(DateUtils.getStartOfMonth(1, year));
        endDate.setValue(DateUtils.getEndOfMonth(12, year));
    }
    
    /**
     * Set custom date range for reports
     * @param start Start date in milliseconds
     * @param end End date in milliseconds
     */
    public void setCustomDateRange(long start, long end) {
        selectedTimeRange.setValue(3); // Custom
        startDate.setValue(start);
        endDate.setValue(end);
    }

    public LiveData<Double> getTotalExpense() {
        return totalExpense;
    }

    public LiveData<List<ExpenseDao.CategoryTotal>> getCategoryTotals() {
        return categoryTotals;
    }

    public MutableLiveData<Long> getStartDate() {
        return startDate;
    }

    public MutableLiveData<Long> getEndDate() {
        return endDate;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up observers
        startDate.removeObserver(date -> {});
        endDate.removeObserver(date -> {});
    }
}
