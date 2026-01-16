package com.smartbudget.app.presentation.addexpense;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.smartbudget.app.data.local.entity.CategoryEntity;
import com.smartbudget.app.data.local.entity.ExpenseEntity;
import com.smartbudget.app.data.repository.CategoryRepository;
import com.smartbudget.app.data.repository.ExpenseRepository;

import java.util.List;

/**
 * ViewModel for AddExpenseFragment.
 * Uses Transformations.switchMap to avoid nested observers.
 */
public class AddExpenseViewModel extends AndroidViewModel {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    private final MutableLiveData<Boolean> isExpenseType = new MutableLiveData<>(true);
    private final MutableLiveData<Long> selectedCategoryId = new MutableLiveData<>();
    private final MutableLiveData<Long> selectedDate = new MutableLiveData<>(System.currentTimeMillis());
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /**
     * Categories LiveData that automatically switches based on isExpenseType.
     * Uses Transformations.switchMap to avoid nested observers (memory leak).
     */
    private final LiveData<List<CategoryEntity>> currentCategories;
    
    /**
     * MediatorLiveData for loading expense to edit - prevents observeForever memory leaks.
     */
    private final MediatorLiveData<ExpenseEntity> expenseToEdit = new MediatorLiveData<>();

    private long editingExpenseId = -1;

    public AddExpenseViewModel(@NonNull Application application) {
        super(application);
        expenseRepository = new ExpenseRepository(application);
        categoryRepository = new CategoryRepository(application);
        
        // Initialize currentCategories with switchMap - auto-switches when isExpenseType changes
        currentCategories = Transformations.switchMap(isExpenseType, isExpense -> {
            if (isExpense != null && isExpense) {
                return categoryRepository.getExpenseCategories();
            } else {
                return categoryRepository.getIncomeCategories();
            }
        });
    }
    
    /**
     * Gets categories based on current expense type (expense/income).
     * This LiveData automatically updates when isExpenseType changes.
     * 
     * @return LiveData that emits categories for current type
     */
    public LiveData<List<CategoryEntity>> getCurrentCategories() {
        return currentCategories;
    }

    public LiveData<List<CategoryEntity>> getExpenseCategories() {
        return categoryRepository.getExpenseCategories();
    }

    public LiveData<List<CategoryEntity>> getIncomeCategories() {
        return categoryRepository.getIncomeCategories();
    }

    public LiveData<List<CategoryEntity>> getCategoriesByType(boolean isExpense) {
        return categoryRepository.getCategoriesByType(isExpense ? 0 : 1);
    }

    public MutableLiveData<Boolean> getIsExpenseType() {
        return isExpenseType;
    }

    public void setIsExpenseType(boolean isExpense) {
        isExpenseType.setValue(isExpense);
    }

    public MutableLiveData<Long> getSelectedCategoryId() {
        return selectedCategoryId;
    }

    public void setSelectedCategoryId(long categoryId) {
        selectedCategoryId.setValue(categoryId);
    }

    public MutableLiveData<Long> getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(long date) {
        selectedDate.setValue(date);
    }

    public MutableLiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setEditingExpenseId(long id) {
        this.editingExpenseId = id;
    }

    /**
     * Loads an expense for editing.
     * Uses MediatorLiveData to properly observe without memory leaks.
     * 
     * @param expenseId The ID of the expense to load
     */
    public void loadExpenseForEdit(long expenseId) {
        // Use MediatorLiveData to observe once and auto-remove
        LiveData<ExpenseEntity> source = expenseRepository.getExpenseById(expenseId);
        expenseToEdit.addSource(source, expense -> {
            if (expense != null) {
                editingExpenseId = expense.getId();
                selectedCategoryId.setValue(expense.getCategoryId());
                selectedDate.setValue(expense.getDate());
                // Remove source after loading to prevent memory leak
                expenseToEdit.removeSource(source);
            }
        });
    }

    public void saveExpense(double amount, String note) {
        // Validate
        if (amount <= 0) {
            errorMessage.setValue("Vui lòng nhập số tiền hợp lệ");
            return;
        }

        Long categoryId = selectedCategoryId.getValue();
        if (categoryId == null) {
            errorMessage.setValue("Vui lòng chọn danh mục");
            return;
        }

        Long date = selectedDate.getValue();
        if (date == null) {
            date = System.currentTimeMillis();
        }

        ExpenseEntity expense = new ExpenseEntity();
        expense.setAmount(amount);
        expense.setCategoryId(categoryId);
        expense.setDate(date);
        expense.setNote(note);

        if (editingExpenseId > 0) {
            expense.setId(editingExpenseId);
            expenseRepository.update(expense);
        } else {
            expenseRepository.insert(expense);
        }

        saveSuccess.setValue(true);
    }

    public void deleteExpense() {
        if (editingExpenseId > 0) {
            expenseRepository.deleteById(editingExpenseId);
            saveSuccess.setValue(true);
        }
    }
}
