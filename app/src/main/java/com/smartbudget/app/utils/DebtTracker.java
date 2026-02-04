package com.smartbudget.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Debt tracker.
 * Track and manage debts with payoff strategies.
 */
public class DebtTracker {

    private static final String PREFS_NAME = "debt_prefs";
    private static final String KEY_DEBTS = "debts";

    public enum DebtType {
        CREDIT_CARD("💳", "Thẻ tín dụng"),
        LOAN("🏦", "Khoản vay"),
        MORTGAGE("🏠", "Vay mua nhà"),
        CAR_LOAN("🚗", "Vay mua xe"),
        STUDENT_LOAN("🎓", "Vay học phí"),
        PERSONAL("👤", "Vay cá nhân"),
        OTHER("📝", "Khác");

        public final String emoji;
        public final String displayName;

        DebtType(String emoji, String displayName) {
            this.emoji = emoji;
            this.displayName = displayName;
        }
    }

    public static class Debt {
        public String id;
        public String name;
        public DebtType type;
        public double originalAmount;
        public double remainingAmount;
        public double interestRate; // Annual %
        public double minimumPayment;
        public int dayOfMonth; // Payment due day
        public long createdAt;

        public Debt(String name, DebtType type, double originalAmount, 
                   double interestRate, double minimumPayment, int dayOfMonth) {
            this.id = String.valueOf(System.currentTimeMillis());
            this.name = name;
            this.type = type;
            this.originalAmount = originalAmount;
            this.remainingAmount = originalAmount;
            this.interestRate = interestRate;
            this.minimumPayment = minimumPayment;
            this.dayOfMonth = dayOfMonth;
            this.createdAt = System.currentTimeMillis();
        }

        public double getMonthlyInterest() {
            return remainingAmount * (interestRate / 100 / 12);
        }

        public int getMonthsToPayoff() {
            if (minimumPayment <= getMonthlyInterest()) return -1; // Never
            double monthlyPrincipal = minimumPayment - getMonthlyInterest();
            return (int) Math.ceil(remainingAmount / monthlyPrincipal);
        }

        public double getProgressPercent() {
            if (originalAmount <= 0) return 100;
            return ((originalAmount - remainingAmount) / originalAmount) * 100;
        }
    }

    public enum PayoffStrategy {
        AVALANCHE("⚡", "Lãi suất cao trước", "Tiết kiệm tiền lãi nhiều nhất"),
        SNOWBALL("❄️", "Số dư nhỏ trước", "Động lực tốt hơn khi xóa nợ nhanh");

        public final String emoji;
        public final String name;
        public final String description;

        PayoffStrategy(String emoji, String name, String description) {
            this.emoji = emoji;
            this.name = name;
            this.description = description;
        }
    }

    private Context context;
    private SharedPreferences prefs;
    private Gson gson;

    public DebtTracker(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public List<Debt> getAllDebts() {
        String json = prefs.getString(KEY_DEBTS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<Debt>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void addDebt(Debt debt) {
        List<Debt> debts = getAllDebts();
        debts.add(debt);
        saveDebts(debts);
    }

    public void makePayment(String debtId, double amount) {
        List<Debt> debts = getAllDebts();
        for (Debt d : debts) {
            if (d.id.equals(debtId)) {
                d.remainingAmount = Math.max(0, d.remainingAmount - amount);
                break;
            }
        }
        saveDebts(debts);
    }

    public void deleteDebt(String debtId) {
        List<Debt> debts = getAllDebts();
        debts.removeIf(d -> d.id.equals(debtId));
        saveDebts(debts);
    }

    /**
     * Get total debt amount.
     */
    public double getTotalDebt() {
        double total = 0;
        for (Debt d : getAllDebts()) {
            total += d.remainingAmount;
        }
        return total;
    }

    /**
     * Get total monthly minimum payments.
     */
    public double getTotalMinimumPayments() {
        double total = 0;
        for (Debt d : getAllDebts()) {
            total += d.minimumPayment;
        }
        return total;
    }

    /**
     * Get debt payoff order based on strategy.
     */
    public List<Debt> getPayoffOrder(PayoffStrategy strategy) {
        List<Debt> debts = new ArrayList<>(getAllDebts());
        
        if (strategy == PayoffStrategy.AVALANCHE) {
            debts.sort((a, b) -> Double.compare(b.interestRate, a.interestRate));
        } else {
            debts.sort((a, b) -> Double.compare(a.remainingAmount, b.remainingAmount));
        }
        
        return debts;
    }

    /**
     * Calculate debt-free date.
     */
    public String getDebtFreeDate() {
        int maxMonths = 0;
        for (Debt d : getAllDebts()) {
            int months = d.getMonthsToPayoff();
            if (months < 0) return "Không xác định";
            maxMonths = Math.max(maxMonths, months);
        }
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.MONTH, maxMonths);
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/yyyy", 
                new java.util.Locale("vi", "VN"));
        return sdf.format(cal.getTime());
    }

    private void saveDebts(List<Debt> debts) {
        String json = gson.toJson(debts);
        prefs.edit().putString(KEY_DEBTS, json).apply();
    }
}
