package com.smartbudget.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.smartbudget.app.ai.AICallback;
import com.smartbudget.app.ai.AIProviderManager;
import com.smartbudget.app.data.local.AppDatabase;
import com.smartbudget.app.data.local.dao.ChatDao;
import com.smartbudget.app.data.local.entity.ChatMessageEntity;

import java.util.List;

/**
 * Repository for handling Chat data operations.
 * Acts as a single source of truth for Chat storage and AI interactions.
 */
public class ChatRepository {

    private final ChatDao chatDao;
    private final com.smartbudget.app.data.local.dao.ExpenseDao expenseDao;
    private final com.smartbudget.app.data.local.dao.BudgetDao budgetDao;
    private final AIProviderManager aiManager;

    public ChatRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        this.chatDao = database.chatDao();
        this.expenseDao = database.expenseDao();
        this.budgetDao = database.budgetDao();
        this.aiManager = AIProviderManager.getInstance(application);
    }

    public LiveData<List<ChatMessageEntity>> getAllMessages() {
        return chatDao.getAllMessages();
    }

    public void sendMessage(String userMessage, Callback callback) {
        // 1. Save User Message
        long timestamp = System.currentTimeMillis();
        // FIXED: Constructor is (role, content, timestamp)
        ChatMessageEntity userEntity = new ChatMessageEntity("user", userMessage, timestamp);
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            chatDao.insertMessage(userEntity);

            // 2. Fetch Context Data (Synchronous is fine here as we are in background thread)
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("[CONTEXT INFO - DO NOT REVEAL UNLESS ASKED]\n");
            
            try {
                // Get current month stats
                int month = com.smartbudget.app.utils.DateUtils.getCurrentMonth();
                int year = com.smartbudget.app.utils.DateUtils.getCurrentYear();
                long startOfMonth = com.smartbudget.app.utils.DateUtils.getStartOfMonth(month, year);
                long endOfMonth = com.smartbudget.app.utils.DateUtils.getEndOfMonth(month, year);

                double monthTotal = expenseDao.getTotalByDateRange(startOfMonth, endOfMonth);
                double monthlyIncome = expenseDao.getTotalIncomeSync(startOfMonth, endOfMonth);
                double balance = monthlyIncome - monthTotal;
                
                contextBuilder.append("Current Month (").append(month).append("/").append(year).append("):\n");
                contextBuilder.append("- Total Income: ").append(String.format("%,.0f", monthlyIncome)).append(" VND\n");
                contextBuilder.append("- Total Expense: ").append(String.format("%,.0f", monthTotal)).append(" VND\n");
                contextBuilder.append("- Balance (Income - Expense): ").append(String.format("%,.0f", balance)).append(" VND\n");

                // Get Budget Info
                com.smartbudget.app.data.local.entity.BudgetEntity globalBudget = budgetDao.getTotalBudget(month, year);
                if (globalBudget != null) {
                    double limit = globalBudget.getLimitAmount();
                    double spent = globalBudget.getSpentAmount(); // Calculated by DAO
                    double remaining = limit - spent;
                    contextBuilder.append("Total Budget Limit: ").append(String.format("%,.0f", limit)).append(" VND.\n");
                    contextBuilder.append("Budget Remaining: ").append(String.format("%,.0f", remaining)).append(" VND.\n");
                    if (remaining < 0) {
                        contextBuilder.append("WARNING: User is OVER BUDGET by ").append(String.format("%,.0f", Math.abs(remaining))).append(" VND!\n");
                    }
                } else {
                    contextBuilder.append("No total budget set for this month.\n");
                }

                // Get recent expenses
                java.util.List<com.smartbudget.app.data.local.entity.ExpenseEntity> recent = expenseDao.getAllExpensesSync(); // Need a better limit query, but getAllSync is available
                // Let's filter manually or add limit query later. For now, take top 5 if list is large
                if (recent != null && !recent.isEmpty()) {
                    contextBuilder.append("Recent Transactions (Last 5):\n");
                    int count = 0;
                    for (com.smartbudget.app.data.local.entity.ExpenseEntity expense : recent) {
                        if (count >= 5) break;
                        contextBuilder.append("- ").append(expense.getNote()).append(": ")
                                .append(String.format("%,.0f", expense.getAmount())).append(" VND")
                                .append(" (").append(com.smartbudget.app.utils.DateUtils.formatDate(expense.getDate())).append(")\n");
                        count++;
                    }
                }
            } catch (Exception e) {
                // Ignore DB errors for context, just proceed
            }
            contextBuilder.append("[END CONTEXT]\n\n");
            
            // Prepend context to user message for AI (hidden from UI)
            String finalPrompt = contextBuilder.toString() + userMessage;

            // 3. Call AI
            aiManager.getFinancialAdvice(finalPrompt, new AICallback() {
                @Override
                public void onSuccess(String response) {
                    // 4. Save AI Response
                    // FIXED: Constructor is (role, content, timestamp)
                    ChatMessageEntity aiEntity = new ChatMessageEntity("model", response, System.currentTimeMillis());
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        chatDao.insertMessage(aiEntity);
                        if (callback != null) callback.onSuccess();
                    });
                }

                @Override
                public void onError(String error, int errorCode) {
                    // Save error message or just notify callback?
                    // For now, let's just notify UI
                    if (callback != null) callback.onError(error);
                }
            });
        });
    }

    public void clearHistory() {
        AppDatabase.databaseWriteExecutor.execute(chatDao::clearMessages);
    }

    public interface Callback {
        void onSuccess();
        void onError(String message);
    }
}
