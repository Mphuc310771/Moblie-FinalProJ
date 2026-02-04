package com.smartbudget.app.presentation.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.smartbudget.app.R;
import com.smartbudget.app.data.local.entity.CategoryEntity;
import com.smartbudget.app.data.local.entity.ExpenseEntity;
import com.smartbudget.app.databinding.FragmentDashboardBinding;
import com.smartbudget.app.presentation.adapter.ExpenseAdapter;
import com.smartbudget.app.presentation.chat.ChatActivity;
import com.smartbudget.app.presentation.scan.ScanReceiptActivity;
import com.smartbudget.app.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private ExpenseAdapter expenseAdapter;

    private Map<Long, CategoryEntity> categoryMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private List<com.smartbudget.app.data.local.dao.ExpenseDao.CategoryTotal> currentTotals;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupRecyclerView();
        setupPieChart();
        setupQuickActions();
        setupSwipeRefresh();
        setupHeaderActions();
        updateGreeting();
        observeData();
    }

    private void setupHeaderActions() {
        // Help button - shows feature guide
        if (binding.ivHelp != null) {
            binding.ivHelp.setOnClickListener(v -> showFeatureGuide());
        }
        
        // Settings button (iv_notification uses settings icon)
        if (binding.ivNotification != null) {
            binding.ivNotification.setOnClickListener(v -> {
                // Navigate to Settings tab
                if (getActivity() != null) {
                    com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                        getActivity().findViewById(R.id.bottom_navigation);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.settingsFragment);
                    }
                }
            });
        }
        
        // See All button - navigate to full transactions list
        if (binding.tvSeeAll != null) {
            binding.tvSeeAll.setOnClickListener(v -> {
                // Navigate to Transactions fragment
                androidx.navigation.Navigation.findNavController(requireView())
                    .navigate(R.id.transactionsFragment);
            });
        }
    }

    private void showFeatureGuide() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = 
            new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_feature_guide, null);
        dialog.setContentView(view);
        
        // Close button
        view.findViewById(R.id.btn_close).setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void setupSwipeRefresh() {
        if (binding.swipeRefresh == null) return;
        
        // Set refresh colors
        binding.swipeRefresh.setColorSchemeResources(
                R.color.income_color,
                R.color.expense_color,
                R.color.colorPrimary
        );

        binding.swipeRefresh.setOnRefreshListener(() -> {
            // Haptic feedback
            com.smartbudget.app.utils.HapticHelper.lightClick(binding.getRoot());
            
            // Refresh data - ViewModel LiveData will auto-update
            // Just simulate delay for UX
            binding.getRoot().postDelayed(() -> {
                if (binding != null) {
                    binding.swipeRefresh.setRefreshing(false);
                    if (binding.pieChart != null) {
                        com.smartbudget.app.utils.AnimationHelper.pulse(binding.pieChart);
                    }
                }
            }, 1000);
        });
    }

    /**
     * Show confetti celebration.
     * Call when user achieves a savings goal.
     */
    public void showCelebration() {
        if (binding.konfettiView != null) {
            com.smartbudget.app.utils.ConfettiHelper.celebrate(binding.konfettiView);
            com.smartbudget.app.utils.HapticHelper.celebrate(requireContext());
        }
    }

    private void updateGreeting() {
        if (binding == null || binding.tvGreeting == null) return;

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        String greeting;

        if (hour >= 5 && hour < 11) {
            greeting = "Chào buổi sáng,";
        } else if (hour >= 11 && hour < 14) {
            greeting = "Chào buổi trưa,";
        } else if (hour >= 14 && hour < 18) {
            greeting = "Chào buổi chiều,";
        } else {
            greeting = "Chào buổi tối,";
        }

        binding.tvGreeting.setText(greeting);
        
        // Update User Name
        if (binding.tvUserName != null) {
            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                binding.tvUserName.setText(user.getDisplayName());
            } else {
                binding.tvUserName.setText("Bạn");
            }
        }

        // Update Streak Badge
        updateStreakBadge();

        // Update Motivational Quote
        updateQuote();
    }

    private void updateStreakBadge() {
        if (binding.tvStreakCount == null || binding.tvStreakEmoji == null) return;

        int streak = com.smartbudget.app.utils.StreakHelper.updateStreak(requireContext());
        binding.tvStreakCount.setText(String.valueOf(streak));
        binding.tvStreakEmoji.setText(com.smartbudget.app.utils.StreakHelper.getStreakEmoji(streak));

        // Animate badge on first load
        com.smartbudget.app.utils.AnimationHelper.popIn(binding.streakBadge);

        // Show celebration for milestone streaks
        if (streak == 7 || streak == 14 || streak == 30 || streak == 100 || streak == 365) {
            showCelebration();
        }
    }

    private void updateQuote() {
        if (binding.tvQuote == null) return;
        binding.tvQuote.setText(com.smartbudget.app.utils.MotivationalQuotes.getQuoteOfTheDay());
    }

    /**
     * Launcher for scan receipt result from Dashboard.
     * Navigates to AddExpenseFragment with extracted data.
     */
    private final androidx.activity.result.ActivityResultLauncher<Intent> scanLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    double amount = data.getDoubleExtra("amount", 0);
                    String merchant = data.getStringExtra("merchant");
                    long date = data.getLongExtra("date", System.currentTimeMillis());
                    
                    // Note can be description (legacy) or items list (new AI)
                    String note = data.getStringExtra("note"); 
                    if (note == null) note = merchant; // Fallback

                    // Navigate to Add Expense with arguments
                    Bundle args = new Bundle();
                    args.putDouble("initialAmount", amount);
                    args.putString("initialNote", note);
                    args.putLong("initialDate", date);
                    
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_dashboard_to_addExpense, args);
                }
            }
    );

    private void setupQuickActions() {
        // AI Chat
        binding.btnQuickAi.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ChatActivity.class));
        });

        // Scan Receipt - Use launcher to handle result
        binding.btnQuickScan.setOnClickListener(v -> {
            scanLauncher.launch(new Intent(requireContext(), ScanReceiptActivity.class));
        });

        // Savings Goals
        binding.btnQuickSavings.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.savingsGoalFragment);
        });
    }

    private void setupRecyclerView() {
        expenseAdapter = new ExpenseAdapter(categoryMap);
        expenseAdapter.setOnItemClickListener(expense -> {
            // Navigate to edit
            Bundle args = new Bundle();
            args.putLong("expenseId", expense.getId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_dashboard_to_addExpense, args);
        });

        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTransactions.setAdapter(expenseAdapter);

        // Add staggered animation
        binding.rvTransactions.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());

        // Setup swipe to delete
        setupSwipeToDelete();
    }

    private void setupSwipeToDelete() {
        new androidx.recyclerview.widget.ItemTouchHelper(new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
                0, androidx.recyclerview.widget.ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView,
                                  @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder,
                                  @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                com.smartbudget.app.data.local.entity.ExpenseEntity expense = expenseAdapter.getExpenseAt(position);

                // Haptic feedback
                com.smartbudget.app.utils.HapticHelper.confirm(requireContext());

                // Delete expense
                viewModel.deleteExpense(expense);

                // Show undo snackbar
                com.google.android.material.snackbar.Snackbar.make(
                        binding.getRoot(),
                        "Đã xóa giao dịch 🗑️",
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).setAction("Hoàn tác", v -> {
                    // Undo delete - re-insert
                    viewModel.insertExpense(expense);
                    com.smartbudget.app.utils.HapticHelper.success(requireContext());
                }).show();
            }

            @Override
            public void onChildDraw(@NonNull android.graphics.Canvas c,
                                    @NonNull androidx.recyclerview.widget.RecyclerView recyclerView,
                                    @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // Draw red background with trash icon
                new it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator.Builder(
                        c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.expense_color))
                        .addActionIcon(android.R.drawable.ic_menu_delete)
                        .addCornerRadius(android.util.TypedValue.COMPLEX_UNIT_DIP, 12)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(binding.rvTransactions);
    }

    private void setupPieChart() {
        PieChart chart = binding.pieChart;
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.TRANSPARENT);
        
        // Donut Style
        chart.setHoleRadius(85f); // Thin modern ring
        chart.setTransparentCircleRadius(88f);
        chart.setDrawCenterText(true);
        chart.setCenterText("Tổng quan");
        chart.setCenterTextSize(18f);
        chart.setCenterTextColor(getResources().getColor(R.color.text_primary));
        chart.setCenterTextTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        
        // Disable legend on chart (we have list below)
        chart.getLegend().setEnabled(false); 
        chart.setDrawEntryLabels(false); // Clean look
        
        // Animation
        chart.animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
        
        // Touch
        chart.setTouchEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        chart.setRotationEnabled(true);
    }

    private void observeData() {
        // Observe categories first
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                categoryMap.clear();
                for (CategoryEntity cat : categories) {
                    categoryMap.put(cat.getId(), cat);
                }
                expenseAdapter.setCategoryMap(categoryMap);
                
                // Retry chart update if totals arrived before categories
                if (currentTotals != null) {
                     updatePieChart(currentTotals);
                }
            }
        });

        // Observe recent expenses
        viewModel.getRecentExpenses().observe(getViewLifecycleOwner(), expenses -> {
            if (expenses != null) {
                binding.tvRecentExpensesTitle.setVisibility(expenses.isEmpty() ? View.GONE : View.VISIBLE);
                expenseAdapter.updateExpenses(expenses);
            }
        });

        // Observe category totals for pie chart
        viewModel.getCategoryTotals().observe(getViewLifecycleOwner(), totals -> {
            if (totals != null && !totals.isEmpty()) {
                currentTotals = totals; // Save for retry
                updatePieChart(totals);
                
                // Update center text with total
                double total = 0;
                for (com.smartbudget.app.data.local.dao.ExpenseDao.CategoryTotal t : totals) {
                    total += t.total;
                }
                
                android.text.SpannableString centerText = new android.text.SpannableString(
                    "Tổng chi\n" + CurrencyUtils.formatVND(total));
                
                // Style "Tổng chi" (small, gray)
                centerText.setSpan(new android.text.style.RelativeSizeSpan(0.9f), 0, 8, 0);
                centerText.setSpan(new android.text.style.ForegroundColorSpan(
                    getResources().getColor(R.color.text_secondary)), 0, 8, 0);
                    
                // Style Amount (Bold, Primary)
                centerText.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 9, centerText.length(), 0);
                centerText.setSpan(new android.text.style.ForegroundColorSpan(
                    getResources().getColor(R.color.text_primary)), 9, centerText.length(), 0);
                    
                binding.pieChart.setCenterText(centerText);
            }
        });

        // Observe insight
        viewModel.getSpendingInsight().observe(getViewLifecycleOwner(), insight -> {
            if (binding.tvAiInsight != null) {
                binding.tvAiInsight.setText(insight);
            }
        });

        // Observe Income, Expense and Balance for balance card
        viewModel.getTotalIncome().observe(getViewLifecycleOwner(), income -> {
            if (binding.tvIncome != null) {
                double value = income != null ? income : 0;
                binding.tvIncome.setText("+" + CurrencyUtils.formatVND(value));
            }
        });

        viewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense -> {
            if (binding.tvExpense != null) {
                double value = expense != null ? expense : 0;
                binding.tvExpense.setText("-" + CurrencyUtils.formatVND(value));
            }
        });

        viewModel.getBalance().observe(getViewLifecycleOwner(), balance -> {
            if (binding.tvTotalBalance != null) {
                double value = balance != null ? balance : 0;
                binding.tvTotalBalance.setText(CurrencyUtils.formatVND(value));
            }
        });
    }

    /**
     * Updates the pie chart with category totals.
     * Uses ChartColorUtils for centralized color management.
     *
     * @param totals List of category totals from database
     */
    private void updatePieChart(List<com.smartbudget.app.data.local.dao.ExpenseDao.CategoryTotal> totals) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // Use centralized chart colors from ChartColorUtils
        int[] chartColors = com.smartbudget.app.utils.ChartColorUtils.getChartColors(requireContext());

        int colorIndex = 0;
        for (com.smartbudget.app.data.local.dao.ExpenseDao.CategoryTotal total : totals) {
            CategoryEntity cat = categoryMap.get(total.categoryId);
            String label = cat != null ? cat.getName() : getString(R.string.cat_other);
            entries.add(new PieEntry((float) total.total, label));

            // Use category color or fallback to chart colors
            if (cat != null && cat.getColor() != null) {
                colors.add(com.smartbudget.app.utils.ChartColorUtils.parseColorSafe(
                        cat.getColor(), requireContext()));
            } else {
                colors.add(chartColors[colorIndex % chartColors.length]);
            }
            colorIndex++;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f); // Space between slices
        dataSet.setSelectionShift(8f); // Shift on tap
        dataSet.setDrawValues(false); // Hide values on chart, show in list below

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
