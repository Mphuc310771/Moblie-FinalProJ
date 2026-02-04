package com.smartbudget.app.presentation.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.smartbudget.app.R;
import com.smartbudget.app.data.local.entity.CategoryEntity;
import com.smartbudget.app.data.local.entity.ExpenseEntity;
import com.smartbudget.app.utils.CurrencyUtils;
import com.smartbudget.app.utils.DateUtils;

import java.util.Map;

public class ExpenseAdapter extends ListAdapter<ExpenseEntity, ExpenseAdapter.ExpenseViewHolder> {

    private Map<Long, CategoryEntity> categoryMap;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ExpenseEntity expense);
    }

    public interface OnItemSwipeListener {
        void onItemSwiped(ExpenseEntity expense, int position);
    }

    private OnItemSwipeListener swipeListener;

    public ExpenseAdapter(Map<Long, CategoryEntity> categoryMap) {
        super(DIFF_CALLBACK);
        this.categoryMap = categoryMap;
    }

    public void setCategoryMap(Map<Long, CategoryEntity> categoryMap) {
        this.categoryMap = categoryMap;
        notifyDataSetChanged();
    }

    public void updateExpenses(java.util.List<ExpenseEntity> expenses) {
        // Create new list to force DiffUtil to properly detect changes
        submitList(expenses == null ? null : new java.util.ArrayList<>(expenses));
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemSwipeListener(OnItemSwipeListener listener) {
        this.swipeListener = listener;
    }

    public ExpenseEntity getExpenseAt(int position) {
        return getItem(position);
    }

    private static final DiffUtil.ItemCallback<ExpenseEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<ExpenseEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull ExpenseEntity oldItem, @NonNull ExpenseEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ExpenseEntity oldItem, @NonNull ExpenseEntity newItem) {
            return oldItem.getAmount() == newItem.getAmount() &&
                    oldItem.getDate() == newItem.getDate() &&
                    (oldItem.getNote() == null ? newItem.getNote() == null
                            : oldItem.getNote().equals(newItem.getNote()));
        }
    };

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseEntity expense = getItem(position);
        holder.bind(expense, categoryMap, listener);
        
        // Add staggered animation
        com.smartbudget.app.utils.AnimationHelper.animateListItem(holder, position);
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvCategoryIcon;
        private final TextView tvCategoryName;
        private final TextView tvNote;
        private final TextView tvAmount;
        private final TextView tvDate;
        private final FrameLayout iconBackground;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryIcon = itemView.findViewById(R.id.tv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvNote = itemView.findViewById(R.id.tv_note);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDate = itemView.findViewById(R.id.tv_date);
            iconBackground = (FrameLayout) tvCategoryIcon.getParent();
        }

        public void bind(ExpenseEntity expense, Map<Long, CategoryEntity> categoryMap,
                OnItemClickListener listener) {
            // Get category info
            CategoryEntity category = null;
            if (expense.getCategoryId() != null && categoryMap != null) {
                category = categoryMap.get(expense.getCategoryId());
            }

            if (category != null) {
                tvCategoryIcon.setText(category.getIcon());
                tvCategoryName.setText(category.getName());

                // Set icon background color
                try {
                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setShape(GradientDrawable.OVAL);
                    drawable.setColor(Color.parseColor(category.getColor()) & 0x33FFFFFF | 0x33000000);
                    iconBackground.setBackground(drawable);
                } catch (Exception e) {
                    // Use default background
                }

                // Set amount color based on type
                if (category.isExpense()) {
                    tvAmount.setText(CurrencyUtils.formatVNDWithSign(expense.getAmount(), true));
                    tvAmount.setTextColor(androidx.core.content.ContextCompat.getColor(
                            itemView.getContext(), R.color.expense_color));
                } else {
                    tvAmount.setText(CurrencyUtils.formatVNDWithSign(expense.getAmount(), false));
                    tvAmount.setTextColor(androidx.core.content.ContextCompat.getColor(
                            itemView.getContext(), R.color.income_color));
                }
            } else {
                tvCategoryIcon.setText("📦");
                tvCategoryName.setText("Khác");
                tvAmount.setText(CurrencyUtils.formatVND(expense.getAmount()));
            }

            // Set note
            String note = expense.getNote();
            if (note != null && !note.isEmpty()) {
                tvNote.setText(note);
                tvNote.setVisibility(View.VISIBLE);
            } else {
                tvNote.setVisibility(View.GONE);
            }

            // Set date
            tvDate.setText(DateUtils.getRelativeDate(expense.getDate()));

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(expense);
                }
            });
        }
    }
}
