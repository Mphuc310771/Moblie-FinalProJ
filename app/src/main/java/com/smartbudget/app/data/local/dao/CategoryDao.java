package com.smartbudget.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.smartbudget.app.data.local.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    long insert(CategoryEntity category);

    @Insert
    void insertAll(List<CategoryEntity> categories);

    @Update
    void update(CategoryEntity category);

    @Delete
    void delete(CategoryEntity category);

    @Query("SELECT * FROM categories ORDER BY type, name")
    LiveData<List<CategoryEntity>> getAllCategories();

    @Query("SELECT * FROM categories WHERE type = :type GROUP BY name ORDER BY name")
    LiveData<List<CategoryEntity>> getCategoriesByType(int type);

    @Query("SELECT * FROM categories WHERE type = 0 GROUP BY name ORDER BY name")
    LiveData<List<CategoryEntity>> getExpenseCategories();

    @Query("SELECT * FROM categories WHERE type = 1 GROUP BY name ORDER BY name")
    LiveData<List<CategoryEntity>> getIncomeCategories();

    @Query("SELECT * FROM categories WHERE id = :id")
    CategoryEntity getCategoryById(long id);

    @Query("SELECT * FROM categories WHERE id = :id")
    LiveData<CategoryEntity> getCategoryByIdLive(long id);

    @Query("SELECT COUNT(*) FROM categories")
    int getCategoryCount();

    @Query("DELETE FROM categories WHERE isCustom = 1")
    void deleteCustomCategories();

    @Query("SELECT * FROM categories ORDER BY type, name")
    List<CategoryEntity> getAllCategoriesSync();
}
