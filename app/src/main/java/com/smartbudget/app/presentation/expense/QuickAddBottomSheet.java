package com.smartbudget.app.presentation.expense;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.smartbudget.app.R;
import com.smartbudget.app.data.local.entity.ExpenseEntity;
import com.smartbudget.app.data.repository.ExpenseRepository;
import com.smartbudget.app.databinding.BottomSheetQuickAddBinding;
import com.smartbudget.app.utils.HapticHelper;

/**
 * Quick add expense bottom sheet.
 * Allows fast expense entry with predefined amounts and categories.
 */
public class QuickAddBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetQuickAddBinding binding;
    private ExpenseRepository expenseRepository;
    private Long selectedCategoryId = null;
    private double selectedAmount = 0;

    // Category IDs (match your database)
    private static final long CAT_FOOD = 1L;
    private static final long CAT_TRANSPORT = 2L;
    private static final long CAT_SHOPPING = 3L;
    private static final long CAT_COFFEE = 4L;
    private static final long CAT_ENTERTAINMENT = 5L;

    public static QuickAddBottomSheet newInstance() {
        return new QuickAddBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetQuickAddBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        expenseRepository = new ExpenseRepository(requireActivity().getApplication());

        setupAmountChips();
        setupCategorySelection();
        setupSaveButton();
    }

    private void setupAmountChips() {
        binding.chip10k.setOnClickListener(v -> setAmount(10000));
        binding.chip20k.setOnClickListener(v -> setAmount(20000));
        binding.chip50k.setOnClickListener(v -> setAmount(50000));
        binding.chip100k.setOnClickListener(v -> setAmount(100000));
        binding.chip200k.setOnClickListener(v -> setAmount(200000));
    }

    private void setAmount(double amount) {
        selectedAmount = amount;
        binding.etAmount.setText(String.valueOf((int) amount));
        HapticHelper.lightClick(binding.getRoot());
    }

    private void setupCategorySelection() {
        View.OnClickListener categoryClickListener = v -> {
            // Clear previous selection
            clearCategorySelection();

            // Set new selection
            v.setSelected(true);
            HapticHelper.lightClick(v);

            if (v.getId() == R.id.cat_food) {
                selectedCategoryId = CAT_FOOD;
            } else if (v.getId() == R.id.cat_transport) {
                selectedCategoryId = CAT_TRANSPORT;
            } else if (v.getId() == R.id.cat_shopping) {
                selectedCategoryId = CAT_SHOPPING;
            } else if (v.getId() == R.id.cat_coffee) {
                selectedCategoryId = CAT_COFFEE;
            } else if (v.getId() == R.id.cat_entertainment) {
                selectedCategoryId = CAT_ENTERTAINMENT;
            }
        };

        binding.catFood.setOnClickListener(categoryClickListener);
        binding.catTransport.setOnClickListener(categoryClickListener);
        binding.catShopping.setOnClickListener(categoryClickListener);
        binding.catCoffee.setOnClickListener(categoryClickListener);
        binding.catEntertainment.setOnClickListener(categoryClickListener);
    }

    private void clearCategorySelection() {
        binding.catFood.setSelected(false);
        binding.catTransport.setSelected(false);
        binding.catShopping.setSelected(false);
        binding.catCoffee.setSelected(false);
        binding.catEntertainment.setSelected(false);
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> {
            String amountStr = binding.etAmount.getText().toString().trim();
            
            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "Nhập số tiền!", Toast.LENGTH_SHORT).show();
                com.smartbudget.app.utils.AnimationHelper.shake(binding.etAmount);
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCategoryId == null) {
                Toast.makeText(requireContext(), "Chọn danh mục!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create and save expense
            ExpenseEntity expense = new ExpenseEntity();
            expense.setAmount(amount);
            expense.setCategoryId(selectedCategoryId);
            expense.setNote(binding.etNote.getText().toString().trim());
            expense.setDate(System.currentTimeMillis());

            expenseRepository.insert(expense);

            // Success feedback
            HapticHelper.success(requireContext());
            Toast.makeText(requireContext(), "✅ Đã lưu!", Toast.LENGTH_SHORT).show();

            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
