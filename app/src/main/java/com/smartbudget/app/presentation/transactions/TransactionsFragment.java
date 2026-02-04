package com.smartbudget.app.presentation.transactions;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.smartbudget.app.R;
import com.smartbudget.app.data.local.entity.CategoryEntity;
import com.smartbudget.app.data.local.entity.ExpenseEntity;
import com.smartbudget.app.databinding.FragmentTransactionsBinding;
import com.smartbudget.app.presentation.adapter.ExpenseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionsFragment extends Fragment {

    private FragmentTransactionsBinding binding;
    private TransactionsViewModel viewModel;
    private ExpenseAdapter adapter;
    private Map<Long, CategoryEntity> categoryMap = new HashMap<>();
    private List<ExpenseEntity> allExpenses = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TransactionsViewModel.class);

        setupRecyclerView();
        setupSwipeToDelete();
        setupSearch();
        setupListeners();
        observeData();
    }

    private void setupRecyclerView() {
        adapter = new ExpenseAdapter(categoryMap);
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTransactions.setAdapter(adapter);

        // Click to edit
        adapter.setOnItemClickListener(expense -> {
            Bundle args = new Bundle();
            args.putLong("expenseId", expense.getId());
            androidx.navigation.Navigation.findNavController(requireView())
                    .navigate(R.id.addExpenseFragment, args);
        });
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                    @NonNull RecyclerView.ViewHolder viewHolder,
                    @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ExpenseEntity expense = adapter.getExpenseAt(position);

                // Haptic feedback
                com.smartbudget.app.utils.HapticHelper.confirm(requireContext());

                // Delete expense
                viewModel.deleteExpense(expense);

                // Show undo snackbar
                Snackbar.make(binding.getRoot(), "Đã xóa giao dịch 🗑️", Snackbar.LENGTH_LONG)
                        .setAction("Hoàn tác", v -> {
                            viewModel.insertExpense(expense);
                            com.smartbudget.app.utils.HapticHelper.success(requireContext());
                        }).show();
            }

            @Override
            public void onChildDraw(@NonNull android.graphics.Canvas c,
                    @NonNull RecyclerView recyclerView,
                    @NonNull RecyclerView.ViewHolder viewHolder,
                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator.Builder(
                        c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(androidx.core.content.ContextCompat.getColor(
                                requireContext(), R.color.expense_color))
                        .addActionIcon(R.drawable.ic_delete)
                        .addCornerRadius(1, 12)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(binding.rvTransactions);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterExpenses(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterExpenses(String query) {
        if (query.isEmpty()) {
            adapter.updateExpenses(allExpenses);
            updateCount(allExpenses.size());
        } else {
            List<ExpenseEntity> filtered = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (ExpenseEntity expense : allExpenses) {
                String note = expense.getNote() != null ? expense.getNote().toLowerCase() : "";
                CategoryEntity cat = categoryMap.get(expense.getCategoryId());
                String catName = cat != null ? cat.getName().toLowerCase() : "";
                
                if (note.contains(lowerQuery) || catName.contains(lowerQuery)) {
                    filtered.add(expense);
                }
            }
            adapter.updateExpenses(filtered);
            updateCount(filtered.size());
        }
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
    }

    private void observeData() {
        // Observe categories
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                categoryMap.clear();
                for (CategoryEntity cat : categories) {
                    categoryMap.put(cat.getId(), cat);
                }
                adapter.setCategoryMap(categoryMap);
            }
        });

        // Observe all expenses
        viewModel.getAllExpenses().observe(getViewLifecycleOwner(), expenses -> {
            if (expenses != null) {
                allExpenses = new ArrayList<>(expenses);
                adapter.updateExpenses(expenses);
                updateCount(expenses.size());
                
                // Show/hide empty state
                binding.emptyState.setVisibility(expenses.isEmpty() ? View.VISIBLE : View.GONE);
                binding.rvTransactions.setVisibility(expenses.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void updateCount(int count) {
        binding.tvCount.setText(count + " giao dịch");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
