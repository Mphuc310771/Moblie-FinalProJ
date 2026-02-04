package com.smartbudget.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Category manager.
 * Allows custom categories with icons and colors.
 */
public class CategoryManager {

    private static final String PREFS_NAME = "category_prefs";
    private static final String KEY_CATEGORIES = "custom_categories";

    public static class Category {
        public String id;
        public String name;
        public String emoji;
        public String color;
        public boolean isDefault;
        public int usageCount;

        public Category(String name, String emoji, String color, boolean isDefault) {
            this.id = String.valueOf(System.currentTimeMillis());
            this.name = name;
            this.emoji = emoji;
            this.color = color;
            this.isDefault = isDefault;
            this.usageCount = 0;
        }
    }

    // Default categories
    public static final Category[] DEFAULT_CATEGORIES = {
            new Category("Ăn uống", "🍔", "#F44336", true),
            new Category("Di chuyển", "🚗", "#2196F3", true),
            new Category("Mua sắm", "🛒", "#9C27B0", true),
            new Category("Giải trí", "🎬", "#FF9800", true),
            new Category("Hóa đơn", "💡", "#607D8B", true),
            new Category("Y tế", "💊", "#E91E63", true),
            new Category("Giáo dục", "📚", "#3F51B5", true),
            new Category("Gia đình", "👨‍👩‍👧", "#4CAF50", true),
            new Category("Làm đẹp", "💄", "#FF4081", true),
            new Category("Thể thao", "⚽", "#00BCD4", true),
            new Category("Quà tặng", "🎁", "#673AB7", true),
            new Category("Khác", "✨", "#9E9E9E", true)
    };

    private Context context;
    private SharedPreferences prefs;
    private Gson gson;

    public CategoryManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Get all categories (default + custom).
     */
    public List<Category> getAllCategories() {
        List<Category> all = new ArrayList<>();
        
        // Add defaults
        for (Category c : DEFAULT_CATEGORIES) {
            all.add(c);
        }
        
        // Add custom
        all.addAll(getCustomCategories());
        
        return all;
    }

    /**
     * Get custom categories only.
     */
    public List<Category> getCustomCategories() {
        String json = prefs.getString(KEY_CATEGORIES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Category>>(){}.getType();
        return gson.fromJson(json, type);
    }

    /**
     * Add a new custom category.
     */
    public void addCategory(Category category) {
        List<Category> categories = getCustomCategories();
        categories.add(category);
        saveCategories(categories);
    }

    /**
     * Update a category.
     */
    public void updateCategory(String categoryId, String newName, String newEmoji, String newColor) {
        List<Category> categories = getCustomCategories();
        for (Category c : categories) {
            if (c.id.equals(categoryId)) {
                c.name = newName;
                c.emoji = newEmoji;
                c.color = newColor;
                break;
            }
        }
        saveCategories(categories);
    }

    /**
     * Delete a category.
     */
    public void deleteCategory(String categoryId) {
        List<Category> categories = getCustomCategories();
        categories.removeIf(c -> c.id.equals(categoryId) && !c.isDefault);
        saveCategories(categories);
    }

    /**
     * Increment usage count for sorting.
     */
    public void incrementUsage(String categoryName) {
        List<Category> categories = getCustomCategories();
        for (Category c : categories) {
            if (c.name.equals(categoryName)) {
                c.usageCount++;
                break;
            }
        }
        saveCategories(categories);
    }

    /**
     * Get most used categories.
     */
    public List<Category> getMostUsedCategories(int limit) {
        List<Category> all = new ArrayList<>(getAllCategories());
        all.sort((a, b) -> Integer.compare(b.usageCount, a.usageCount));
        
        if (all.size() > limit) {
            return all.subList(0, limit);
        }
        return all;
    }

    /**
     * Find category by name.
     */
    public Category findByName(String name) {
        for (Category c : getAllCategories()) {
            if (c.name.equalsIgnoreCase(name)) {
                return c;
            }
        }
        return DEFAULT_CATEGORIES[DEFAULT_CATEGORIES.length - 1]; // Return "Khác"
    }

    private void saveCategories(List<Category> categories) {
        String json = gson.toJson(categories);
        prefs.edit().putString(KEY_CATEGORIES, json).apply();
    }
}
