package com.smartbudget.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Entity for savings goals
 * Allows users to set and track savings targets
 */
@Entity(tableName = "savings_goals")
public class SavingsGoalEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String icon;
    private String color;
    private double targetAmount;
    private double currentAmount;
    private long deadline; // timestamp
    private long createdAt;
    private boolean isCompleted;

    public SavingsGoalEntity() {
        this.createdAt = System.currentTimeMillis();
        this.currentAmount = 0;
        this.isCompleted = false;
    }

    @Ignore
    public SavingsGoalEntity(String name, double targetAmount, long deadline) {
        this();
        this.name = name;
        this.targetAmount = targetAmount;
        this.deadline = deadline;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }

    public long getDeadline() { return deadline; }
    public void setDeadline(long deadline) { this.deadline = deadline; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    // Helper methods
    public int getProgressPercentage() {
        if (targetAmount <= 0) return 0;
        int progress = (int) ((currentAmount / targetAmount) * 100);
        return Math.min(progress, 100);
    }

    public double getRemainingAmount() {
        return Math.max(0, targetAmount - currentAmount);
    }

    public boolean isOverdue() {
        return !isCompleted && System.currentTimeMillis() > deadline;
    }
}
