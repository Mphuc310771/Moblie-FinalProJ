package com.smartbudget.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.smartbudget.app.data.local.AppDatabase;
import com.smartbudget.app.data.local.dao.CategoryDao;
import com.smartbudget.app.data.local.entity.CategoryEntity;

import java.util.List;

public class CategoryRepository {

    private final CategoryDao categoryDao;

    public CategoryRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        categoryDao = database.categoryDao();
    }

    // Insert
    public void insert(CategoryEntity category) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            categoryDao.insert(category);
        });
    }

    // Update
    public void update(CategoryEntity category) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            categoryDao.update(category);
        });
    }

    // Delete
    public void delete(CategoryEntity category) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            categoryDao.delete(category);
        });
    }

    // Get all categories
    public LiveData<List<CategoryEntity>> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    // Get expense categories
    public LiveData<List<CategoryEntity>> getExpenseCategories() {
        return categoryDao.getExpenseCategories();
    }

    // Get income categories
    public LiveData<List<CategoryEntity>> getIncomeCategories() {
        return categoryDao.getIncomeCategories();
    }

    // Get by type
    public LiveData<List<CategoryEntity>> getCategoriesByType(int type) {
        return categoryDao.getCategoriesByType(type);
    }

    // Get by ID
    public LiveData<CategoryEntity> getCategoryById(long id) {
        return categoryDao.getCategoryByIdLive(id);
    }
}
