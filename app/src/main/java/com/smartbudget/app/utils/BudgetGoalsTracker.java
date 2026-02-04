package com.smartbudget.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Budget goals tracker.
 * Track and manage savings goals with progress.
 */
public class BudgetGoalsTracker {

    private static final String PREFS_NAME = "budget_goals";
    private static final String KEY_GOALS = "goals";

    public static class Goal {
        public String id;
        public String name;
        public String emoji;
        public double targetAmount;
        public double currentAmount;
        public long deadline; // timestamp
        public long createdAt;
        public boolean isCompleted;

        public Goal(String name, String emoji, double targetAmount, long deadline) {
            this.id = String.valueOf(System.currentTimeMillis());
            this.name = name;
            this.emoji = emoji;
            this.targetAmount = targetAmount;
            this.currentAmount = 0;
            this.deadline = deadline;
            this.createdAt = System.currentTimeMillis();
            this.isCompleted = false;
        }

        public float getProgress() {
            if (targetAmount <= 0) return 0f;
            return (float) (currentAmount / targetAmount);
        }

        public double getRemainingAmount() {
            return Math.max(0, targetAmount - currentAmount);
        }

        public int getDaysRemaining() {
            long remaining = deadline - System.currentTimeMillis();
            return (int) (remaining / (24 * 60 * 60 * 1000));
        }

        public double getDailyTarget() {
            int days = getDaysRemaining();
            if (days <= 0) return getRemainingAmount();
            return getRemainingAmount() / days;
        }
    }

    // Pre-defined goal templates
    public static final String[][] GOAL_TEMPLATES = {
            {"🏖️", "Du lịch"},
            {"📱", "iPhone mới"},
            {"💻", "Laptop mới"},
            {"🏠", "Mua nhà"},
            {"🚗", "Mua xe"},
            {"💍", "Đám cưới"},
            {"🎓", "Học phí"},
            {"💰", "Quỹ khẩn cấp"},
            {"🎁", "Quà sinh nhật"},
            {"🏥", "Quỹ y tế"}
    };

    private Context context;
    private SharedPreferences prefs;
    private Gson gson;

    public BudgetGoalsTracker(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Get all goals.
     */
    public List<Goal> getAllGoals() {
        String json = prefs.getString(KEY_GOALS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Goal>>(){}.getType();
        return gson.fromJson(json, type);
    }

    /**
     * Get active (non-completed) goals.
     */
    public List<Goal> getActiveGoals() {
        List<Goal> all = getAllGoals();
        List<Goal> active = new ArrayList<>();
        for (Goal g : all) {
            if (!g.isCompleted) {
                active.add(g);
            }
        }
        return active;
    }

    /**
     * Add a new goal.
     */
    public void addGoal(Goal goal) {
        List<Goal> goals = getAllGoals();
        goals.add(goal);
        saveGoals(goals);
    }

    /**
     * Update goal progress.
     */
    public void addProgress(String goalId, double amount) {
        List<Goal> goals = getAllGoals();
        for (Goal g : goals) {
            if (g.id.equals(goalId)) {
                g.currentAmount += amount;
                if (g.currentAmount >= g.targetAmount) {
                    g.isCompleted = true;
                }
                break;
            }
        }
        saveGoals(goals);
    }

    /**
     * Delete a goal.
     */
    public void deleteGoal(String goalId) {
        List<Goal> goals = getAllGoals();
        goals.removeIf(g -> g.id.equals(goalId));
        saveGoals(goals);
    }

    /**
     * Get total savings needed.
     */
    public double getTotalRemainingAmount() {
        double total = 0;
        for (Goal g : getActiveGoals()) {
            total += g.getRemainingAmount();
        }
        return total;
    }

    /**
     * Get motivational message based on progress.
     */
    public String getMotivationalMessage() {
        List<Goal> active = getActiveGoals();
        if (active.isEmpty()) {
            return "🎯 Hãy đặt mục tiêu tiết kiệm đầu tiên!";
        }

        Goal nearestGoal = null;
        float maxProgress = 0;
        for (Goal g : active) {
            if (g.getProgress() > maxProgress) {
                maxProgress = g.getProgress();
                nearestGoal = g;
            }
        }

        if (nearestGoal != null) {
            int percent = (int) (maxProgress * 100);
            if (percent >= 90) {
                return String.format("🎉 Sắp đạt %s! Còn %,.0f₫", nearestGoal.name, nearestGoal.getRemainingAmount());
            } else if (percent >= 50) {
                return String.format("💪 Đã đạt %d%% mục tiêu %s!", percent, nearestGoal.name);
            } else {
                return String.format("🚀 Tiếp tục tiết kiệm cho %s!", nearestGoal.name);
            }
        }

        return "💰 Tiếp tục tiết kiệm nhé!";
    }

    private void saveGoals(List<Goal> goals) {
        String json = gson.toJson(goals);
        prefs.edit().putString(KEY_GOALS, json).apply();
    }
}
