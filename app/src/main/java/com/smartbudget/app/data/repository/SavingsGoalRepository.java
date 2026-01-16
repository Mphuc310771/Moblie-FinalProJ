package com.smartbudget.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.smartbudget.app.data.local.AppDatabase;
import com.smartbudget.app.data.local.dao.SavingsGoalDao;
import com.smartbudget.app.data.local.entity.SavingsGoalEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository class for managing Savings Goal data operations.
 * 
 * This class provides a clean API for data access to the rest of the application.
 * It abstracts the data source from the UI layer and provides methods for
 * all CRUD operations on SavingsGoalEntity.
 * 
 * Features:
 * - LiveData support for reactive UI updates
 * - Background thread execution for database operations
 * - Goal progress tracking and completion management
 * - Total savings calculation
 * 
 * @author SmartBudget Development Team
 * @version 1.0
 * @since 2025-01
 */
public class SavingsGoalRepository {

    /** Data Access Object for savings goals */
    private final SavingsGoalDao savingsGoalDao;
    
    /** Executor service for background database operations */
    private final ExecutorService executorService;

    /** LiveData containing all savings goals */
    private final LiveData<List<SavingsGoalEntity>> allGoals;
    
    /** LiveData containing only active (non-completed) goals */
    private final LiveData<List<SavingsGoalEntity>> activeGoals;
    
    /** LiveData containing only completed goals */
    private final LiveData<List<SavingsGoalEntity>> completedGoals;
    
    /** LiveData containing total savings across all goals */
    private final LiveData<Double> totalSavings;
    
    /** LiveData containing count of active goals */
    private final LiveData<Integer> activeGoalCount;

    /**
     * Constructor initializes the repository with application context.
     * Sets up the DAO and fetches initial LiveData objects.
     * 
     * @param application The application context for database initialization
     */
    public SavingsGoalRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        savingsGoalDao = database.savingsGoalDao();
        executorService = Executors.newFixedThreadPool(2);
        
        allGoals = savingsGoalDao.getAllGoals();
        activeGoals = savingsGoalDao.getActiveGoals();
        completedGoals = savingsGoalDao.getCompletedGoals();
        totalSavings = savingsGoalDao.getTotalSavings();
        activeGoalCount = savingsGoalDao.getActiveGoalCount();
    }

    // ==================== CREATE Operations ====================

    /**
     * Inserts a new savings goal into the database.
     * Operation is performed on a background thread.
     * 
     * @param goal The SavingsGoalEntity to insert
     */
    public void insert(SavingsGoalEntity goal) {
        executorService.execute(() -> savingsGoalDao.insert(goal));
    }

    /**
     * Inserts a new savings goal and returns the generated ID via callback.
     * Useful when you need to perform actions with the new goal immediately.
     * 
     * @param goal The SavingsGoalEntity to insert
     * @param callback Callback to receive the generated ID
     */
    public void insertWithCallback(SavingsGoalEntity goal, InsertCallback callback) {
        executorService.execute(() -> {
            long id = savingsGoalDao.insert(goal);
            if (callback != null) {
                callback.onInserted(id);
            }
        });
    }

    // ==================== READ Operations ====================

    /**
     * Gets all savings goals as LiveData.
     * Goals are ordered by completion status and deadline.
     * 
     * @return LiveData containing list of all savings goals
     */
    public LiveData<List<SavingsGoalEntity>> getAllGoals() {
        return allGoals;
    }

    /**
     * Gets only active (non-completed) savings goals as LiveData.
     * 
     * @return LiveData containing list of active goals
     */
    public LiveData<List<SavingsGoalEntity>> getActiveGoals() {
        return activeGoals;
    }

    /**
     * Gets only completed savings goals as LiveData.
     * 
     * @return LiveData containing list of completed goals
     */
    public LiveData<List<SavingsGoalEntity>> getCompletedGoals() {
        return completedGoals;
    }

    /**
     * Gets a specific savings goal by its ID.
     * This is a synchronous operation - should be called from background thread.
     * 
     * @param id The ID of the goal to retrieve
     * @return The SavingsGoalEntity, or null if not found
     */
    public SavingsGoalEntity getGoalById(long id) {
        return savingsGoalDao.getGoalById(id);
    }

    /**
     * Gets a specific savings goal by ID as LiveData for reactive updates.
     * 
     * @param id The ID of the goal to retrieve
     * @return LiveData containing the SavingsGoalEntity
     */
    public LiveData<SavingsGoalEntity> getGoalByIdLive(long id) {
        return savingsGoalDao.getGoalByIdLive(id);
    }

    /**
     * Gets the total amount saved across all goals (active and completed).
     * 
     * @return LiveData containing total savings as Double
     */
    public LiveData<Double> getTotalSavings() {
        return totalSavings;
    }

    /**
     * Gets the count of active (non-completed) goals.
     * 
     * @return LiveData containing the count as Integer
     */
    public LiveData<Integer> getActiveGoalCount() {
        return activeGoalCount;
    }

    // ==================== UPDATE Operations ====================

    /**
     * Updates an existing savings goal in the database.
     * Operation is performed on a background thread.
     * 
     * @param goal The SavingsGoalEntity with updated values
     */
    public void update(SavingsGoalEntity goal) {
        executorService.execute(() -> savingsGoalDao.update(goal));
    }

    /**
     * Adds a specified amount to a goal's current savings.
     * This is the primary method for recording savings contributions.
     * 
     * @param goalId The ID of the goal to contribute to
     * @param amount The amount to add to current savings
     */
    public void addSavings(long goalId, double amount) {
        executorService.execute(() -> savingsGoalDao.addSavings(goalId, amount));
    }

    /**
     * Updates goal progress with new amount and completion status.
     * Handles both the savings amount update and completion flag.
     * 
     * @param goalId The ID of the goal to update
     * @param newAmount The new total saved amount
     * @param isCompleted Whether the goal should be marked as completed
     */
    public void updateProgress(long goalId, double newAmount, boolean isCompleted) {
        executorService.execute(() -> 
            savingsGoalDao.updateProgress(goalId, newAmount, isCompleted)
        );
    }

    /**
     * Marks a savings goal as completed.
     * Should be called when currentAmount >= targetAmount.
     * 
     * @param goalId The ID of the goal to mark as completed
     */
    public void markAsCompleted(long goalId) {
        executorService.execute(() -> savingsGoalDao.markAsCompleted(goalId));
    }

    // ==================== DELETE Operations ====================

    /**
     * Deletes a savings goal from the database.
     * Operation is performed on a background thread.
     * 
     * @param goal The SavingsGoalEntity to delete
     */
    public void delete(SavingsGoalEntity goal) {
        executorService.execute(() -> savingsGoalDao.delete(goal));
    }

    // ==================== SYNC Operations ====================

    /**
     * Gets all active goals synchronously (for sync operations).
     * Must be called from a background thread.
     * 
     * @return List of active SavingsGoalEntity objects
     */
    public List<SavingsGoalEntity> getActiveGoalsSync() {
        return savingsGoalDao.getActiveGoalsSync();
    }

    /**
     * Gets all completed goals synchronously.
     * Must be called from a background thread.
     * 
     * @return List of completed SavingsGoalEntity objects
     */
    public List<SavingsGoalEntity> getCompletedGoalsSync() {
        return savingsGoalDao.getCompletedGoalsSync();
    }

    /**
     * Gets total saved amount synchronously.
     * Must be called from a background thread.
     * 
     * @return Total saved amount as Double
     */
    public Double getTotalSavedSync() {
        return savingsGoalDao.getTotalSavedSync();
    }

    // ==================== HELPER Methods ====================

    /**
     * Calculates the progress percentage for a given goal.
     * 
     * @param goal The savings goal to calculate progress for
     * @return Progress percentage (0-100)
     */
    public static int calculateProgressPercentage(SavingsGoalEntity goal) {
        if (goal == null || goal.getTargetAmount() <= 0) {
            return 0;
        }
        double percentage = (goal.getCurrentAmount() / goal.getTargetAmount()) * 100;
        return Math.min(100, (int) percentage);
    }

    /**
     * Calculates remaining amount needed to complete a goal.
     * 
     * @param goal The savings goal
     * @return Remaining amount needed, 0 if goal is completed
     */
    public static double calculateRemainingAmount(SavingsGoalEntity goal) {
        if (goal == null) {
            return 0;
        }
        double remaining = goal.getTargetAmount() - goal.getCurrentAmount();
        return Math.max(0, remaining);
    }

    /**
     * Calculates days remaining until goal deadline.
     * 
     * @param goal The savings goal
     * @return Days remaining, -1 if deadline has passed
     */
    public static long calculateDaysRemaining(SavingsGoalEntity goal) {
        if (goal == null) {
            return 0;
        }
        long now = System.currentTimeMillis();
        long deadline = goal.getDeadline();
        long diff = deadline - now;
        
        if (diff < 0) {
            return -1; // Deadline passed
        }
        
        return diff / (1000 * 60 * 60 * 24); // Convert to days
    }

    /**
     * Calculates recommended daily/weekly saving amount to meet deadline.
     * 
     * @param goal The savings goal
     * @param isWeekly If true, returns weekly amount; if false, daily amount
     * @return Recommended saving amount per period
     */
    public static double calculateRecommendedSavingRate(SavingsGoalEntity goal, boolean isWeekly) {
        if (goal == null || goal.isCompleted()) {
            return 0;
        }
        
        long daysRemaining = calculateDaysRemaining(goal);
        if (daysRemaining <= 0) {
            return calculateRemainingAmount(goal); // All remaining amount at once
        }
        
        double remaining = calculateRemainingAmount(goal);
        double dailyRate = remaining / daysRemaining;
        
        return isWeekly ? dailyRate * 7 : dailyRate;
    }

    /**
     * Determines if a goal is at risk of not being completed on time.
     * A goal is considered at risk if progress is less than expected based on time passed.
     * 
     * @param goal The savings goal to check
     * @return true if goal is at risk, false otherwise
     */
    public static boolean isGoalAtRisk(SavingsGoalEntity goal) {
        if (goal == null || goal.isCompleted()) {
            return false;
        }
        
        long now = System.currentTimeMillis();
        long createdAt = goal.getCreatedAt();
        long deadline = goal.getDeadline();
        
        // Calculate expected progress based on time
        double totalDuration = deadline - createdAt;
        double elapsedDuration = now - createdAt;
        
        if (totalDuration <= 0) {
            return true; // Invalid duration
        }
        
        double expectedProgressRatio = elapsedDuration / totalDuration;
        double actualProgressRatio = goal.getCurrentAmount() / goal.getTargetAmount();
        
        // Goal is at risk if actual progress is less than 80% of expected
        return actualProgressRatio < (expectedProgressRatio * 0.8);
    }

    /**
     * Shuts down the executor service.
     * Should be called when the repository is no longer needed.
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    // ==================== Callback Interfaces ====================

    /**
     * Callback interface for insert operations that require the new ID.
     */
    public interface InsertCallback {
        /**
         * Called when the insert operation completes.
         * 
         * @param id The generated ID of the inserted goal
         */
        void onInserted(long id);
    }
}
