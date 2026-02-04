package com.smartbudget.app.presentation.transactions;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.smartbudget.app.data.local.entity.CategoryEntity;
import com.smartbudget.app.data.local.entity.ExpenseEntity;
import com.smartbudget.app.data.repository.CategoryRepository;
import com.smartbudget.app.data.repository.ExpenseRepository;

import java.util.List;

public class TransactionsViewModel extends AndroidViewModel {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public TransactionsViewModel(@NonNull Application application) {
        super(application);
        expenseRepository = new ExpenseRepository(application);
        categoryRepository = new CategoryRepository(application);
    }

    public LiveData<List<ExpenseEntity>> getAllExpenses() {
        return expenseRepository.getAllExpenses();
    }

    public LiveData<List<CategoryEntity>> getCategories() {
        return categoryRepository.getAllCategories();
    }

    public void deleteExpense(ExpenseEntity expense) {
        expenseRepository.delete(expense);
    }

    public void insertExpense(ExpenseEntity expense) {
        expenseRepository.insert(expense);
    }
}
