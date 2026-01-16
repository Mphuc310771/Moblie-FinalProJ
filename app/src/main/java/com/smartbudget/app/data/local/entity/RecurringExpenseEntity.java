package com.smartbudget.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity for recurring/scheduled expenses that repeat on a regular basis.
 * Supports monthly bills, subscriptions, etc.
 */
@Entity(tableName = "recurring_expenses",
        foreignKeys = @ForeignKey(
                entity = CategoryEntity.class,
                parentColumns = "id",
                childColumns = "categoryId",
                onDelete = ForeignKey.SET_NULL),
        indices = {@Index("categoryId")})
public class RecurringExpenseEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private double amount;
    private Long categoryId;
    private int dayOfMonth; // 1-31
    private int frequency; // 0=monthly, 1=weekly, 2=yearly
    private boolean isActive;
    private long nextDueDate;
    private long createdAt;
    private String note;

    // Frequency constants
    public static final int FREQ_MONTHLY = 0;
    public static final int FREQ_WEEKLY = 1;
    public static final int FREQ_YEARLY = 2;

    public RecurringExpenseEntity() {
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.frequency = FREQ_MONTHLY;
    }

    public RecurringExpenseEntity(String name, double amount, int dayOfMonth) {
        this();
        this.name = name;
        this.amount = amount;
        this.dayOfMonth = dayOfMonth;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public int getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(int dayOfMonth) { this.dayOfMonth = dayOfMonth; }

    public int getFrequency() { return frequency; }
    public void setFrequency(int frequency) { this.frequency = frequency; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public long getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(long nextDueDate) { this.nextDueDate = nextDueDate; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getFrequencyText() {
        switch (frequency) {
            case FREQ_WEEKLY: return "Hàng tuần";
            case FREQ_YEARLY: return "Hàng năm";
            default: return "Hàng tháng";
        }
    }
}
