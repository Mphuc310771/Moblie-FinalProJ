package com.smartbudget.app.presentation.reports;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.smartbudget.app.R;
import com.smartbudget.app.data.local.dao.ExpenseDao;
import com.smartbudget.app.data.local.entity.CategoryEntity;
import com.smartbudget.app.databinding.FragmentReportsBinding;
import com.smartbudget.app.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsFragment extends Fragment {

    private FragmentReportsBinding binding;
    private ReportsViewModel viewModel;

    private Map<Long, CategoryEntity> categoryMap = new HashMap<>();
    private List<ExpenseDao.CategoryTotal> currentTotals;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ReportsViewModel.class);

        setupCharts();
        setupListeners();
        observeData();
    }

    private void setupCharts() {
        // Setup Pie Chart
        // Setup Pie Chart
        PieChart pieChart = binding.pieChart;
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(85f); // Thin modern ring
        pieChart.setTransparentCircleRadius(88f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        
        // Legend styling
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setForm(com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE);
        pieChart.getLegend().setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        pieChart.getLegend().setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        pieChart.getLegend().setDrawInside(false);
        pieChart.getLegend().setWordWrapEnabled(true);
        
        pieChart.animateY(1200, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);

        // Setup Bar Chart
        // Setup Bar Chart
        BarChart barChart = binding.barChart;
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setDrawBarShadow(false);
        barChart.getLegend().setEnabled(false);
        barChart.animateY(1200, com.github.mikephil.charting.animation.Easing.EaseOutQuart);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setDrawAxisLine(false); // Clean look
        xAxis.setTextColor(getResources().getColor(R.color.text_secondary));

        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setGridColor(Color.parseColor("#1A000000")); // Very light grid
        barChart.getAxisLeft().setDrawAxisLine(false);
        barChart.getAxisLeft().setDrawLabels(false); // Hide Y-axis labels for cleaner look
        
        barChart.getAxisRight().setEnabled(false);
    }

    private void setupListeners() {
        binding.toggleTime.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_weekly) {
                    viewModel.setTimeRange(0);
                    binding.cardDateRange.setVisibility(View.GONE);
                } else if (checkedId == R.id.btn_monthly) {
                    viewModel.setTimeRange(1);
                    binding.cardDateRange.setVisibility(View.GONE);
                } else if (checkedId == R.id.btn_yearly) {
                    viewModel.setTimeRange(2);
                    binding.cardDateRange.setVisibility(View.GONE);
                } else if (checkedId == R.id.btn_custom) {
                    viewModel.setTimeRange(3);
                    binding.cardDateRange.setVisibility(View.VISIBLE);
                }
            }
        });

        // Default to monthly
        binding.toggleTime.check(R.id.btn_monthly);

        // Date picker for start date
        binding.btnStartDate.setOnClickListener(v -> {
            showDatePicker(true);
        });

        // Date picker for end date
        binding.btnEndDate.setOnClickListener(v -> {
            showDatePicker(false);
        });
    }

    private long customStartDate = 0;
    private long customEndDate = 0;

    private void showDatePicker(boolean isStartDate) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        
        // Use current selected date if available
        Long currentDate = isStartDate ? viewModel.getStartDate().getValue() : viewModel.getEndDate().getValue();
        if (currentDate != null && currentDate > 0) {
            cal.setTimeInMillis(currentDate);
        }

        android.app.DatePickerDialog dialog = new android.app.DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    java.util.Calendar selectedCal = java.util.Calendar.getInstance();
                    selectedCal.set(year, month, dayOfMonth);
                    
                    if (isStartDate) {
                        customStartDate = com.smartbudget.app.utils.DateUtils.getStartOfDay(selectedCal.getTimeInMillis());
                        binding.btnStartDate.setText(formatDate(customStartDate));
                        
                        // If end date not set or before start date, set end date to start date
                        if (customEndDate == 0 || customEndDate < customStartDate) {
                            customEndDate = com.smartbudget.app.utils.DateUtils.getEndOfDay(selectedCal.getTimeInMillis());
                            binding.btnEndDate.setText(formatDate(customEndDate));
                        }
                    } else {
                        customEndDate = com.smartbudget.app.utils.DateUtils.getEndOfDay(selectedCal.getTimeInMillis());
                        binding.btnEndDate.setText(formatDate(customEndDate));
                        
                        // If start date not set, set start date to end date
                        if (customStartDate == 0) {
                            customStartDate = com.smartbudget.app.utils.DateUtils.getStartOfDay(selectedCal.getTimeInMillis());
                            binding.btnStartDate.setText(formatDate(customStartDate));
                        }
                    }
                    
                    // Update viewmodel with custom range
                    if (customStartDate > 0 && customEndDate > 0) {
                        viewModel.setCustomDateRange(customStartDate, customEndDate);
                    }
                },
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH),
                cal.get(java.util.Calendar.DAY_OF_MONTH)
        );
        
        dialog.show();
    }

    private String formatDate(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }

    private void observeData() {
        // Observe categories
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                categoryMap.clear();
                for (CategoryEntity cat : categories) {
                    categoryMap.put(cat.getId(), cat);
                }
                
                // Retry chart update if totals arrived before categories
                if (currentTotals != null) {
                    updatePieChart(currentTotals);
                    updateBarChart(currentTotals);
                }
            }
        });

        // Observe total expense
        viewModel.getTotalExpense().observe(getViewLifecycleOwner(), total -> {
            if (total != null) {
                binding.tvTotalSpent.setText(CurrencyUtils.formatVND(total));
            }
        });

        // Observe category totals
        viewModel.getCategoryTotals().observe(getViewLifecycleOwner(), totals -> {
            if (totals != null && !totals.isEmpty()) {
                currentTotals = totals; // Save for retry
                updatePieChart(totals);
                updateBarChart(totals);
            }
        });
    }

    /**
     * Cập nhật pie chart với dữ liệu theo danh mục.
     * Sử dụng ChartColorUtils để quản lý màu sắc tập trung.
     *
     * @param totals Danh sách tổng chi tiêu theo danh mục
     */
    private void updatePieChart(List<ExpenseDao.CategoryTotal> totals) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // Sử dụng màu từ ChartColorUtils thay vì hardcode
        int[] chartColors = com.smartbudget.app.utils.ChartColorUtils.getChartColors(requireContext());

        int colorIndex = 0;
        for (ExpenseDao.CategoryTotal total : totals) {
            CategoryEntity cat = categoryMap.get(total.categoryId);
            String label = cat != null ? cat.getName() : getString(R.string.cat_other);
            entries.add(new PieEntry((float) total.total, label));

            // Ưu tiên màu từ category, fallback sang chart colors
            if (cat != null && cat.getColor() != null) {
                colors.add(com.smartbudget.app.utils.ChartColorUtils.parseColorSafe(
                        cat.getColor(), requireContext()));
            } else {
                colors.add(chartColors[colorIndex % chartColors.length]);
            }
            colorIndex++;
        }

        // Create Dataset
        PieDataSet dataSet = new PieDataSet(entries, ""); // No label
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f); // Space between slices
        dataSet.setSelectionShift(8f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setDrawValues(false); // Hide values on chart
        
        PieData pieData = new PieData(dataSet);
        
        // Setup Chart
        binding.pieChart.setData(pieData);
        // Don't need to re-set style here as it's done in setupCharts()
        
        binding.pieChart.animateY(800, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
        binding.pieChart.invalidate();
    }

    /**
     * Cập nhật bar chart với dữ liệu theo danh mục.
     * Hiển thị top categories dưới dạng cột.
     *
     * @param totals Danh sách tổng chi tiêu theo danh mục
     */
    private void updateBarChart(List<ExpenseDao.CategoryTotal> totals) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // Sử dụng màu từ ChartColorUtils
        int[] chartColors = com.smartbudget.app.utils.ChartColorUtils.getChartColors(requireContext());

        int index = 0;
        for (ExpenseDao.CategoryTotal total : totals) {
            entries.add(new BarEntry(index, (float) total.total));

            CategoryEntity cat = categoryMap.get(total.categoryId);
            labels.add(cat != null ? cat.getName() : getString(R.string.cat_other));

            // Ưu tiên màu từ category
            if (cat != null && cat.getColor() != null) {
                colors.add(com.smartbudget.app.utils.ChartColorUtils.parseColorSafe(
                        cat.getColor(), requireContext()));
            } else {
                colors.add(chartColors[index % chartColors.length]);
            }
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.expense));
        dataSet.setColors(colors);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        binding.barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.barChart.setData(data);
        binding.barChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
