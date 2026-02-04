package com.smartbudget.app.presentation.chat;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.smartbudget.app.R;
import com.smartbudget.app.databinding.ActivityChatBinding;

/**
 * Chat activity for AI-powered financial advice.
 * Refactored to MVVM architecture (Step 4).
 */
public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private ChatAdapter chatAdapter;
    private ChatViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        setupUI();
        observeViewModel();
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        // Add Clear History button handling
        binding.btnBack.setOnLongClickListener(v -> {
            showClearHistoryDialog();
            return true;
        });

        // Help button - shows AI guide
        binding.btnHelp.setOnClickListener(v -> showAIGuide());

        // Setup RecyclerView
        chatAdapter = new ChatAdapter();
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(this));
        
        // Auto scroll on new messages
        chatAdapter.registerAdapterDataObserver(new androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                binding.rvMessages.smoothScrollToPosition(chatAdapter.getItemCount());
            }
        });
        
        binding.rvMessages.setAdapter(chatAdapter);

        // Setup send button
        binding.btnSend.setOnClickListener(v -> sendMessage());

        // Setup keyboard send
        binding.etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        // Quick suggestions
        binding.chipBudget.setOnClickListener(v -> 
            askQuestion(getString(R.string.chat_suggestion_budget)));
        binding.chipSave.setOnClickListener(v -> 
            askQuestion(getString(R.string.chat_suggestion_save)));
        binding.chipAnalyze.setOnClickListener(v -> 
            askQuestion(getString(R.string.chat_suggestion_analyze)));
    }

    private void showAIGuide() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = 
            new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_ai_guide, null);
        dialog.setContentView(view);
        
        view.findViewById(R.id.btn_close).setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void observeViewModel() {
        // Observe messages
        viewModel.getMessages().observe(this, messages -> {
            chatAdapter.submitList(messages);
            if (messages.isEmpty()) {
                // We could add a welcome message here if the list is empty,
                // but ChatRepository/DB might handle initial state.
                // For now, if empty, we can show a hint or welcome via Adapter logic or View.
                // Let's rely on empty state in UI if needed, or initial DB population.
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnSend.setEnabled(!isLoading);
            if (isLoading) {
                binding.chipGroup.setVisibility(View.GONE);
            }
        });

        // Observe error
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, getString(R.string.error_prefix) + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        viewModel.sendMessage(text);
        binding.etMessage.setText("");
    }

    private void askQuestion(String question) {
        binding.etMessage.setText(question);
        sendMessage();
    }

    private void showClearHistoryDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.chat_clear_title)
            .setMessage(R.string.chat_clear_message)
            .setPositiveButton(R.string.delete, (d, w) -> viewModel.clearHistory())
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
}
