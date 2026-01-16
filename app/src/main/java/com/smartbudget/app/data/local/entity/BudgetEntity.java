package com.smartbudget.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets", foreignKeys = @ForeignKey(entity = CategoryEntity.class, parentColumns = "id", childColumns = "categoryId", onDelete = ForeignKey.CASCADE), indices = {
        @Index("categoryId") })
public class BudgetEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private Long categoryId; // null means total budget
    private double limitAmount;
    private int month; // 1-12
    private int year;
    private double spentAmount;

    public BudgetEntity() {
    }

    public BudgetEntity(Long categoryId, double limitAmount, int month, int year) {
        this.categoryId = categoryId;
        this.limitAmount = limitAmount;
        this.month = month;
        this.year = year;
        this.spentAmount = 0;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public double getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(double limitAmount) {
        this.limitAmount = limitAmount;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public double getRemainingAmount() {
        return limitAmount - spentAmount;
    }

    public int getPercentageUsed() {
        if (limitAmount == 0)
            return 0;
        return (int) ((spentAmount / limitAmount) * 100);
    }

    public boolean isOverBudget() {
        return spentAmount > limitAmount;
    }
}
