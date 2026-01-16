package com.smartbudget.app.data.local;

import android.content.Context;
import android.util.Log;

import com.smartbudget.app.data.local.entity.ExpenseEntity;
import com.smartbudget.app.utils.DateUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Utility to seed the database with sample data for demonstration and testing.
 */
public class DatabaseSeeder {

    private static final String TAG = "DatabaseSeeder";

    public static void seed(Context context) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Check for specific user
            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (user == null || user.getEmail() == null || !user.getEmail().equalsIgnoreCase("dtmpk12@gmail.com")) {
                Log.d(TAG, "Skipping seed: User is not dtmpk12@gmail.com");
                return;
            }

            AppDatabase db = AppDatabase.getInstance(context);
            
            // Check if data already exists
            if (db.expenseDao().getAllExpensesSync().size() > 0) {
                Log.d(TAG, "Database already seeded. Skipping.");
                return;
            }

            Log.d(TAG, "Seeding database for " + user.getEmail() + "...");

            long currentTimestamp = System.currentTimeMillis();
            int currentMonth = DateUtils.getCurrentMonth();
            int currentYear = DateUtils.getCurrentYear();
            long startOfMonth = DateUtils.getStartOfMonth(currentMonth, currentYear);

            // 1. Income: Salary
            ExpenseEntity salary = new ExpenseEntity();
            salary.setAmount(25000000.0);
            salary.setNote("Lương tháng " + currentMonth);
            salary.setDate(startOfMonth + (2 * 24 * 60 * 60 * 1000L));
            salary.setCategoryId(9L); // Income category ID
            // TransactionType is not part of ExpenseEntity, relying on Category type
            db.expenseDao().insert(salary);

            // 2. Expenses
            createExpense(db, 5000000.0, "Tiền thuê nhà", startOfMonth + (5 * 86400000L), 7L);
            createExpense(db, 50000.0, "Cà phê sáng", System.currentTimeMillis() - 86400000L, 1L);
            createExpense(db, 45000.0, "Cơm trưa văn phòng", System.currentTimeMillis() - 86400000L, 1L);
            createExpense(db, 500000.0, "Siêu thị tuần 1", startOfMonth + (7 * 86400000L), 1L);
            createExpense(db, 100000.0, "Grab đi làm", System.currentTimeMillis() - (2 * 86400000L), 2L);
            createExpense(db, 200000.0, "Vé xem phim", System.currentTimeMillis() - (3 * 86400000L), 5L);
            createExpense(db, 1500000.0, "Thanh toán điện nước", startOfMonth + (10 * 86400000L), 8L);

            Log.d(TAG, "Database seeding complete.");
        });
    }

    private static void createExpense(AppDatabase db, double amount, String note, long date, long categoryId) {
        ExpenseEntity expense = new ExpenseEntity();
        expense.setAmount(amount);
        expense.setNote(note);
        expense.setDate(date);
        expense.setCategoryId(categoryId);
        db.expenseDao().insert(expense);
    }
}
