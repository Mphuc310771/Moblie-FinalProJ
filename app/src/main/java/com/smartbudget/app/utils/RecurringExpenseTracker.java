package com.smartbudget.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Recurring expense tracker.
 * Tracks and reminds about recurring expenses.
 */
public class RecurringExpenseTracker {

    private static final String PREFS_NAME = "recurring_expenses";
    private static final String KEY_EXPENSES = "expenses";

    public enum Frequency {
        DAILY("Hàng ngày", 1),
        WEEKLY("Hàng tuần", 7),
        BIWEEKLY("2 tuần/lần", 14),
        MONTHLY("Hàng tháng", 30),
        QUARTERLY("Hàng quý", 90),
        YEARLY("Hàng năm", 365);

        public final String displayName;
        public final int days;

        Frequency(String displayName, int days) {
            this.displayName = displayName;
            this.days = days;
        }
    }

    public static class RecurringExpense {
        public String id;
        public String name;
        public String emoji;
        public double amount;
        public String category;
        public Frequency frequency;
        public int dayOfMonth; // For monthly expenses
        public long nextDueDate;
        public boolean isActive;
        public boolean autoAdd;

        public RecurringExpense(String name, String emoji, double amount, 
                               String category, Frequency frequency, int dayOfMonth) {
            this.id = String.valueOf(System.currentTimeMillis());
            this.name = name;
            this.emoji = emoji;
            this.amount = amount;
            this.category = category;
            this.frequency = frequency;
            this.dayOfMonth = dayOfMonth;
            this.isActive = true;
            this.autoAdd = false;
            updateNextDueDate();
        }

        public void updateNextDueDate() {
            Calendar cal = Calendar.getInstance();
            
            if (frequency == Frequency.MONTHLY) {
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
                    cal.add(Calendar.MONTH, 1);
                }
            } else {
                cal.add(Calendar.DAY_OF_MONTH, frequency.days);
            }
            
            nextDueDate = cal.getTimeInMillis();
        }

        public int getDaysUntilDue() {
            long diff = nextDueDate - System.currentTimeMillis();
            return (int) (diff / (24 * 60 * 60 * 1000));
        }

        public boolean isDueToday() {
            return getDaysUntilDue() == 0;
        }

        public boolean isOverdue() {
            return getDaysUntilDue() < 0;
        }
    }

    // Common recurring expenses
    public static final String[][] COMMON_RECURRING = {
            {"💡", "Tiền điện", "500000"},
            {"💧", "Tiền nước", "100000"},
            {"📶", "Internet", "200000"},
            {"📱", "Điện thoại", "150000"},
            {"🏠", "Tiền nhà", "5000000"},
            {"🎬", "Netflix", "180000"},
            {"🎵", "Spotify", "59000"},
            {"💪", "Phí gym", "500000"},
            {"🚗", "Bảo hiểm xe", "1000000"},
            {"🏥", "Bảo hiểm y tế", "500000"}
    };

    private Context context;
    private SharedPreferences prefs;
    private Gson gson;

    public RecurringExpenseTracker(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Get all recurring expenses.
     */
    public List<RecurringExpense> getAllExpenses() {
        String json = prefs.getString(KEY_EXPENSES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<RecurringExpense>>(){}.getType();
        return gson.fromJson(json, type);
    }

    /**
     * Get expenses due today or overdue.
     */
    public List<RecurringExpense> getDueExpenses() {
        List<RecurringExpense> due = new ArrayList<>();
        for (RecurringExpense e : getAllExpenses()) {
            if (e.isActive && (e.isDueToday() || e.isOverdue())) {
                due.add(e);
            }
        }
        return due;
    }

    /**
     * Get upcoming expenses (next 7 days).
     */
    public List<RecurringExpense> getUpcomingExpenses() {
        List<RecurringExpense> upcoming = new ArrayList<>();
        for (RecurringExpense e : getAllExpenses()) {
            int daysUntil = e.getDaysUntilDue();
            if (e.isActive && daysUntil > 0 && daysUntil <= 7) {
                upcoming.add(e);
            }
        }
        return upcoming;
    }

    /**
     * Add a recurring expense.
     */
    public void addExpense(RecurringExpense expense) {
        List<RecurringExpense> expenses = getAllExpenses();
        expenses.add(expense);
        saveExpenses(expenses);
    }

    /**
     * Mark expense as paid and update next due date.
     */
    public void markAsPaid(String expenseId) {
        List<RecurringExpense> expenses = getAllExpenses();
        for (RecurringExpense e : expenses) {
            if (e.id.equals(expenseId)) {
                e.updateNextDueDate();
                break;
            }
        }
        saveExpenses(expenses);
    }

    /**
     * Delete a recurring expense.
     */
    public void deleteExpense(String expenseId) {
        List<RecurringExpense> expenses = getAllExpenses();
        expenses.removeIf(e -> e.id.equals(expenseId));
        saveExpenses(expenses);
    }

    /**
     * Get total monthly recurring amount.
     */
    public double getMonthlyTotal() {
        double total = 0;
        for (RecurringExpense e : getAllExpenses()) {
            if (!e.isActive) continue;
            
            switch (e.frequency) {
                case DAILY:
                    total += e.amount * 30;
                    break;
                case WEEKLY:
                    total += e.amount * 4;
                    break;
                case BIWEEKLY:
                    total += e.amount * 2;
                    break;
                case MONTHLY:
                    total += e.amount;
                    break;
                case QUARTERLY:
                    total += e.amount / 3;
                    break;
                case YEARLY:
                    total += e.amount / 12;
                    break;
            }
        }
        return total;
    }

    private void saveExpenses(List<RecurringExpense> expenses) {
        String json = gson.toJson(expenses);
        prefs.edit().putString(KEY_EXPENSES, json).apply();
    }
}
