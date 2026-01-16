package com.smartbudget.app.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.smartbudget.app.data.local.dao.BudgetDao;
import com.smartbudget.app.data.local.dao.CategoryDao;
import com.smartbudget.app.data.local.dao.ChatDao;
import com.smartbudget.app.data.local.dao.ExpenseDao;
import com.smartbudget.app.data.local.dao.RecurringExpenseDao;
import com.smartbudget.app.data.local.dao.SavingsGoalDao;
import com.smartbudget.app.data.local.entity.BudgetEntity;
import com.smartbudget.app.data.local.entity.CategoryEntity;
import com.smartbudget.app.data.local.entity.ChatMessageEntity;
import com.smartbudget.app.data.local.entity.ExpenseEntity;
import com.smartbudget.app.data.local.entity.RecurringExpenseEntity;
import com.smartbudget.app.data.local.entity.SavingsGoalEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        CategoryEntity.class,
        ExpenseEntity.class,
        BudgetEntity.class,
        SavingsGoalEntity.class,
        ChatMessageEntity.class,
        RecurringExpenseEntity.class
}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CategoryDao categoryDao();

    public abstract ExpenseDao expenseDao();

    public abstract BudgetDao budgetDao();

    public abstract SavingsGoalDao savingsGoalDao();

    public abstract ChatDao chatDao();

    public abstract RecurringExpenseDao recurringExpenseDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Alias for FirebaseSyncHelper
    public static AppDatabase getInstance(final Context context) {
        return getDatabase(context);
    }

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "smartbudget_database")
                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Clear all data from all tables. Used when switching accounts.
     */
    public void clearAllData() {
        databaseWriteExecutor.execute(() -> {
            expenseDao().deleteAll();
            budgetDao().deleteAll();
            savingsGoalDao().deleteAll();
            chatDao().clearMessages();
            recurringExpenseDao().deleteAll();
            // Keep categories as they are default data
        });
    }

    /**
     * Reset database instance (for account switching)
     */
    public static void resetInstance() {
        if (INSTANCE != null) {
            if (INSTANCE.isOpen()) {
                INSTANCE.close();
            }
            INSTANCE = null;
        }
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            populateDatabase();
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            populateDatabase();
        }
    };

    private static void populateDatabase() {
        databaseWriteExecutor.execute(() -> {
            if (INSTANCE != null) {
                CategoryDao dao = INSTANCE.categoryDao();
                if (dao.getCategoryCount() == 0) {
                    List<CategoryEntity> defaultCategories = getDefaultCategories();
                    dao.insertAll(defaultCategories);
                }
            }
        });
    }

    private static List<CategoryEntity> getDefaultCategories() {
        List<CategoryEntity> categories = new ArrayList<>();

        // Expense categories (type = 0)
        categories.add(new CategoryEntity("Ăn uống", "🍔", "#FF6B6B", 0, false));
        categories.add(new CategoryEntity("Di chuyển", "🚗", "#4ECDC4", 0, false));
        categories.add(new CategoryEntity("Mua sắm", "🛒", "#A66CFF", 0, false));
        categories.add(new CategoryEntity("Sức khỏe", "💊", "#FF9F43", 0, false));
        categories.add(new CategoryEntity("Giải trí", "🎮", "#FF6B9D", 0, false));
        categories.add(new CategoryEntity("Học tập", "📚", "#54A0FF", 0, false));
        categories.add(new CategoryEntity("Nhà cửa", "🏠", "#5F27CD", 0, false));
        categories.add(new CategoryEntity("Điện nước", "💡", "#00D2D3", 0, false));
        categories.add(new CategoryEntity("Khác", "📦", "#95979A", 0, false));

        // Income categories (type = 1)
        categories.add(new CategoryEntity("Lương", "💰", "#10AC84", 1, false));
        categories.add(new CategoryEntity("Quà tặng", "🎁", "#EE5A24", 1, false));
        categories.add(new CategoryEntity("Đầu tư", "📈", "#1E90FF", 1, false));
        categories.add(new CategoryEntity("Thưởng", "🏆", "#F368E0", 1, false));
        categories.add(new CategoryEntity("Khác", "💵", "#95979A", 1, false));

        return categories;
    }
}
