package com.smartbudget.app.presentation.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.smartbudget.app.R;
import com.smartbudget.app.data.local.entity.BudgetEntity;
import com.smartbudget.app.data.local.entity.CategoryEntity;
import com.smartbudget.app.utils.CurrencyUtils;

import java.util.Map;

public class BudgetAdapter extends ListAdapter<BudgetEntity, BudgetAdapter.BudgetViewHolder> {

    private Map<Long, CategoryEntity> categoryMap;

    public BudgetAdapter(Map<Long, CategoryEntity> categoryMap) {
        super(DIFF_CALLBACK);
        this.categoryMap = categoryMap;
    }

    public void setCategoryMap(Map<Long, CategoryEntity> categoryMap) {
        this.categoryMap = categoryMap;
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<BudgetEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<BudgetEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull BudgetEntity oldItem, @NonNull BudgetEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull BudgetEntity oldItem, @NonNull BudgetEntity newItem) {
            return oldItem.getLimitAmount() == newItem.getLimitAmount() &&
                    oldItem.getSpentAmount() == newItem.getSpentAmount();
        }
    };

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetEntity budget = getItem(position);
        holder.bind(budget, categoryMap);
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvIcon;
        private final TextView tvCategory;
        private final TextView tvPercentage;
        private final LinearProgressIndicator progressBudget;
        private final TextView tvSpent;
        private final TextView tvLimit;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tv_icon);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvPercentage = itemView.findViewById(R.id.tv_percentage);
            progressBudget = itemView.findViewById(R.id.progress_budget);
            tvSpent = itemView.findViewById(R.id.tv_spent);
            tvLimit = itemView.findViewById(R.id.tv_limit);
        }

        public void bind(BudgetEntity budget, Map<Long, CategoryEntity> categoryMap) {
            // Get category info
            CategoryEntity category = null;
            if (budget.getCategoryId() != null && categoryMap != null) {
                category = categoryMap.get(budget.getCategoryId());
            }

            if (category != null) {
                tvIcon.setText(category.getIcon());
                tvCategory.setText(category.getName());
            } else {
                tvIcon.setText("💰");
                tvCategory.setText("Tổng ngân sách");
            }

            // Set percentage
            int percentage = budget.getPercentageUsed();
            tvPercentage.setText(percentage + "%");

            // Set progress
            progressBudget.setProgress(Math.min(percentage, 100));

            // Set progress color based on usage
            if (percentage >= 100) {
                progressBudget.setIndicatorColor(Color.parseColor("#E74C3C")); // Red
                tvPercentage.setTextColor(Color.parseColor("#E74C3C"));
            } else if (percentage >= 80) {
                progressBudget.setIndicatorColor(Color.parseColor("#F39C12")); // Orange
                tvPercentage.setTextColor(Color.parseColor("#F39C12"));
            } else {
                progressBudget.setIndicatorColor(Color.parseColor("#1E3A5F")); // Primary
                tvPercentage.setTextColor(Color.parseColor("#6C757D"));
            }

            // Set amounts
            tvSpent.setText("Đã chi: " + CurrencyUtils.formatVND(budget.getSpentAmount()));
            tvLimit.setText("Giới hạn: " + CurrencyUtils.formatVND(budget.getLimitAmount()));
        }
    }
}
