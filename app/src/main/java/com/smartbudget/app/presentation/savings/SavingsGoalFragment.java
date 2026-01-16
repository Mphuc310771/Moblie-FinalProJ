package com.smartbudget.app.presentation.savings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.smartbudget.app.R;
import com.smartbudget.app.data.local.AppDatabase;
import com.smartbudget.app.data.local.entity.SavingsGoalEntity;
import com.smartbudget.app.databinding.FragmentSavingsGoalBinding;
import com.smartbudget.app.utils.CurrencyUtils;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SavingsGoalFragment extends Fragment {

    private FragmentSavingsGoalBinding binding;
    private SavingsGoalAdapter adapter;
    private AppDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentSavingsGoalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        database = AppDatabase.getInstance(requireContext());
        
        setupRecyclerView();
        setupListeners();
        loadGoals();
    }

    private void setupRecyclerView() {
        adapter = new SavingsGoalAdapter();
        adapter.setOnAddMoneyClickListener(this::showAddMoneyDialog);
        
        binding.rvGoals.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGoals.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.fabAddGoal.setOnClickListener(v -> showAddGoalDialog());
    }

    private void loadGoals() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<SavingsGoalEntity> activeGoals = database.savingsGoalDao().getActiveGoalsSync();
            List<SavingsGoalEntity> completedGoals = database.savingsGoalDao().getCompletedGoalsSync();
            Double totalSaved = database.savingsGoalDao().getTotalSavedSync();
            
            requireActivity().runOnUiThread(() -> {
                // Update summary
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                binding.tvTotalSaved.setText(formatter.format(totalSaved != null ? totalSaved : 0) + " ₫");
                binding.tvActiveGoals.setText(activeGoals.size() + " mục tiêu đang thực hiện");
                binding.tvCompletedGoals.setText(completedGoals.size() + " đã hoàn thành ✓");
                
                // Show goals
                if (activeGoals.isEmpty()) {
                    binding.rvGoals.setVisibility(View.GONE);
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    binding.rvGoals.setVisibility(View.VISIBLE);
                    binding.layoutEmpty.setVisibility(View.GONE);
                    adapter.submitList(activeGoals);
                }
            });
        });
    }

    private void showAddGoalDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_goal, null);
        
        EditText etName = dialogView.findViewById(R.id.et_goal_name);
        EditText etAmount = dialogView.findViewById(R.id.et_goal_amount);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("🎯 Thêm mục tiêu mới")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String amountStr = etAmount.getText().toString().trim();
                    
                    if (name.isEmpty() || amountStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    double amount = CurrencyUtils.parseAmount(amountStr);
                    // Default deadline: 1 year from now
                    Calendar deadline = Calendar.getInstance();
                    deadline.add(Calendar.YEAR, 1);
                    
                    SavingsGoalEntity goal = new SavingsGoalEntity(name, amount, deadline.getTimeInMillis());
                    goal.setIcon("🎯");
                    
                    Executors.newSingleThreadExecutor().execute(() -> {
                        database.savingsGoalDao().insert(goal);
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Đã thêm mục tiêu!", Toast.LENGTH_SHORT).show();
                            loadGoals();
                        });
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAddMoneyDialog(SavingsGoalEntity goal) {
        EditText etAmount = new EditText(requireContext());
        etAmount.setHint("Nhập số tiền");
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("💰 Thêm tiền vào \"" + goal.getName() + "\"")
                .setView(etAmount)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String amountStr = etAmount.getText().toString().trim();
                    if (amountStr.isEmpty()) return;
                    
                    double amount = Double.parseDouble(amountStr);
                    double newAmount = goal.getCurrentAmount() + amount;
                    boolean isCompleted = newAmount >= goal.getTargetAmount();
                    
                    Executors.newSingleThreadExecutor().execute(() -> {
                        database.savingsGoalDao().updateProgress(goal.getId(), newAmount, isCompleted);
                        requireActivity().runOnUiThread(() -> {
                            if (isCompleted) {
                                Toast.makeText(requireContext(), "🎉 Chúc mừng! Bạn đã hoàn thành mục tiêu!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(requireContext(), "Đã thêm tiền!", Toast.LENGTH_SHORT).show();
                            }
                            loadGoals();
                        });
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
