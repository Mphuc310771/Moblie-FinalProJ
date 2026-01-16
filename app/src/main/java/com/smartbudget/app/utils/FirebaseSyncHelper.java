package com.smartbudget.app.utils;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.smartbudget.app.data.local.AppDatabase;
import com.smartbudget.app.data.local.entity.BudgetEntity;
import com.smartbudget.app.data.local.entity.CategoryEntity;
import com.smartbudget.app.data.local.entity.ExpenseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Helper class for Firebase sync operations
 * Handles uploading local data to Firestore and downloading remote data
 */
public class FirebaseSyncHelper {

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_EXPENSES = "expenses";
    private static final String COLLECTION_CATEGORIES = "categories";
    private static final String COLLECTION_BUDGETS = "budgets";

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private AppDatabase database;
    private Context context;
    private Executor executor;

    public FirebaseSyncHelper(Context context) {
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.database = AppDatabase.getInstance(context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void signOut() {
        auth.signOut();
    }

    /**
     * Upload all local data to Firestore
     */
    public void uploadData(SyncCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("Chưa đăng nhập");
            return;
        }

        String userId = user.getUid();
        
        executor.execute(() -> {
            try {
                // Get all local data
                List<ExpenseEntity> expenses = database.expenseDao().getUnsyncedExpenses();
                List<CategoryEntity> categories = database.categoryDao().getAllCategoriesSync();
                List<BudgetEntity> budgets = database.budgetDao().getAllBudgetsSync();

                // Upload to Firestore
                WriteBatch batch = firestore.batch();

                // Upload expenses
                for (ExpenseEntity expense : expenses) {
                    Map<String, Object> data = expenseToMap(expense);
                    batch.set(
                        firestore.collection(COLLECTION_USERS)
                            .document(userId)
                            .collection(COLLECTION_EXPENSES)
                            .document(String.valueOf(expense.getId())),
                        data
                    );
                }

                // Upload categories
                for (CategoryEntity category : categories) {
                    Map<String, Object> data = categoryToMap(category);
                    batch.set(
                        firestore.collection(COLLECTION_USERS)
                            .document(userId)
                            .collection(COLLECTION_CATEGORIES)
                            .document(String.valueOf(category.getId())),
                        data
                    );
                }

                // Upload budgets
                for (BudgetEntity budget : budgets) {
                    Map<String, Object> data = budgetToMap(budget);
                    batch.set(
                        firestore.collection(COLLECTION_USERS)
                            .document(userId)
                            .collection(COLLECTION_BUDGETS)
                            .document(String.valueOf(budget.getId())),
                        data
                    );
                }

                // Commit batch
                batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        // Mark expenses as synced
                        List<Long> ids = new ArrayList<>();
                        for (ExpenseEntity e : expenses) {
                            ids.add(e.getId());
                        }
                        executor.execute(() -> {
                            database.expenseDao().markAsSynced(ids);
                        });
                        
                        callback.onSuccess("Đã đồng bộ " + expenses.size() + " giao dịch lên cloud");
                    })
                    .addOnFailureListener(e -> {
                        callback.onError("Lỗi đồng bộ: " + e.getMessage());
                    });

            } catch (Exception e) {
                callback.onError("Lỗi: " + e.getMessage());
            }
        });
    }

    /**
     * Download data from Firestore and merge with local
     */
    public void downloadData(SyncCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("Chưa đăng nhập");
            return;
        }

        String userId = user.getUid();

        // Download expenses
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_EXPENSES)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                executor.execute(() -> {
                    int count = 0;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        ExpenseEntity expense = mapToExpense(doc);
                        if (expense != null) {
                            // Check if exists locally
                            ExpenseEntity existing = database.expenseDao().getExpenseById(expense.getId());
                            if (existing == null) {
                                database.expenseDao().insert(expense);
                                count++;
                            }
                        }
                    }
                    
                    final int finalCount = count;
                    callback.onSuccess("Đã tải " + finalCount + " giao dịch từ cloud");
                });
            })
            .addOnFailureListener(e -> {
                callback.onError("Lỗi tải dữ liệu: " + e.getMessage());
            });
    }

    /**
     * Full sync - upload then download
     */
    public void syncAll(SyncCallback callback) {
        uploadData(new SyncCallback() {
            @Override
            public void onSuccess(String message) {
                downloadData(callback);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // Conversion helpers
    private Map<String, Object> expenseToMap(ExpenseEntity expense) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", expense.getId());
        map.put("amount", expense.getAmount());
        map.put("categoryId", expense.getCategoryId());
        map.put("description", expense.getDescription());
        map.put("date", expense.getDate());
        map.put("createdAt", expense.getCreatedAt());
        return map;
    }

    private Map<String, Object> categoryToMap(CategoryEntity category) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", category.getId());
        map.put("name", category.getName());
        map.put("icon", category.getIcon());
        map.put("color", category.getColor());
        map.put("isExpense", category.isExpense());
        return map;
    }

    private Map<String, Object> budgetToMap(BudgetEntity budget) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", budget.getId());
        map.put("categoryId", budget.getCategoryId());
        map.put("limitAmount", budget.getLimitAmount());
        map.put("month", budget.getMonth());
        map.put("year", budget.getYear());
        return map;
    }

    private ExpenseEntity mapToExpense(DocumentSnapshot doc) {
        try {
            ExpenseEntity expense = new ExpenseEntity();
            expense.setId(doc.getLong("id"));
            expense.setAmount(doc.getDouble("amount"));
            expense.setCategoryId(doc.getLong("categoryId"));
            expense.setDescription(doc.getString("description"));
            expense.setDate(doc.getLong("date"));
            expense.setCreatedAt(doc.getLong("createdAt"));
            expense.setSynced(true);
            return expense;
        } catch (Exception e) {
            return null;
        }
    }

    public interface SyncCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}
