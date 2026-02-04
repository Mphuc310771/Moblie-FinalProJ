package com.smartbudget.app.presentation.budget;

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
import com.smartbudget.app.data.local.entity.BudgetEntity;
import com.smartbudget.app.data.local.entity.CategoryEntity;
import com.smartbudget.app.databinding.FragmentBudgetBinding;
import com.smartbudget.app.presentation.adapter.BudgetAdapter;
import com.smartbudget.app.utils.CurrencyUtils;

import java.util.HashMap;
import java.util.Map;

public class BudgetFragment extends Fragment {

    private FragmentBudgetBinding binding;
    private BudgetViewModel viewModel;
    private BudgetAdapter budgetAdapter;

    private Map<Long, CategoryEntity> categoryMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(BudgetViewModel.class);

        setupRecyclerView();
        setupListeners();
        observeData();
    }

    private void setupRecyclerView() {
        budgetAdapter = new BudgetAdapter(categoryMap);
        binding.rvBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBudgets.setAdapter(budgetAdapter);
    }

    private void setupListeners() {
        binding.btnSetBudget.setOnClickListener(v -> showSetBudgetDialog());
        
        // Add category budget button
        if (binding.btnAddCategoryBudget != null) {
            binding.btnAddCategoryBudget.setOnClickListener(v -> showAddCategoryBudgetDialog());
        }
    }

    private void observeData() {
        // Observe categories
        viewModel.getExpenseCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                categoryMap.clear();
                for (CategoryEntity cat : categories) {
                    categoryMap.put(cat.getId(), cat);
                }
                budgetAdapter.setCategoryMap(categoryMap);
            }
        });

        // Observe total budget
        viewModel.getTotalBudget().observe(getViewLifecycleOwner(), budget -> {
            if (budget != null) {
                binding.tvTotalBudget.setText(CurrencyUtils.formatVND(budget.getLimitAmount()));
                binding.progressTotal.setProgress(budget.getPercentageUsed());
                binding.tvSpent.setText("Đã chi: " + CurrencyUtils.formatVND(budget.getSpentAmount()));
                binding.tvRemaining.setText("Còn: " + CurrencyUtils.formatVND(budget.getRemainingAmount()));
            } else {
                binding.tvTotalBudget.setText("Chưa thiết lập");
                binding.progressTotal.setProgress(0);
                binding.tvSpent.setText("Đã chi: 0 ₫");
                binding.tvRemaining.setText("Còn: 0 ₫");
            }
        });

        // Observe monthly budgets (category budgets only - filter out null categoryId)
        viewModel.getCurrentMonthBudgets().observe(getViewLifecycleOwner(), budgets -> {
            if (budgets != null) {
                // Filter to get only category budgets (exclude total budget)
                java.util.List<BudgetEntity> categoryBudgets = new java.util.ArrayList<>();
                for (BudgetEntity b : budgets) {
                    if (b.getCategoryId() != null) {
                        categoryBudgets.add(b);
                    }
                }
                budgetAdapter.submitList(categoryBudgets);
                
                // Show/hide empty state
                if (binding.tvEmptyCategory != null) {
                    binding.tvEmptyCategory.setVisibility(categoryBudgets.isEmpty() ? 
                        android.view.View.VISIBLE : android.view.View.GONE);
                }
            }
        });
    }

    private void showAddCategoryBudgetDialog() {
        // Get list of expense categories
        java.util.List<CategoryEntity> categories = new java.util.ArrayList<>(categoryMap.values());
        if (categories.isEmpty()) {
            Toast.makeText(requireContext(), "Chưa có danh mục chi tiêu", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] categoryNames = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            categoryNames[i] = categories.get(i).getIcon() + " " + categories.get(i).getName();
        }

        final int[] selectedIndex = {0};

        new AlertDialog.Builder(requireContext())
            .setTitle("Chọn danh mục")
            .setSingleChoiceItems(categoryNames, 0, (dialog, which) -> selectedIndex[0] = which)
            .setPositiveButton("Tiếp tục", (dialog, which) -> {
                CategoryEntity selectedCategory = categories.get(selectedIndex[0]);
                showSetCategoryBudgetAmountDialog(selectedCategory);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showSetCategoryBudgetAmountDialog(CategoryEntity category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Ngân sách: " + category.getIcon() + " " + category.getName());

        final EditText input = new EditText(requireContext());
        input.setHint("Nhập số tiền");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String amountStr = input.getText().toString().trim();
            double amount = CurrencyUtils.parseAmount(amountStr);
            if (amount > 0) {
                viewModel.saveCategoryBudget(category.getId(), amount);
                Toast.makeText(requireContext(), "Đã lưu ngân sách " + category.getName(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showSetBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thiết lập ngân sách tháng");

        final EditText input = new EditText(requireContext());
        input.setHint("Nhập số tiền");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String amountStr = input.getText().toString().trim();
            double amount = CurrencyUtils.parseAmount(amountStr);
            if (amount > 0) {
                viewModel.saveTotalBudget(amount);
                Toast.makeText(requireContext(), "Đã lưu ngân sách", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
