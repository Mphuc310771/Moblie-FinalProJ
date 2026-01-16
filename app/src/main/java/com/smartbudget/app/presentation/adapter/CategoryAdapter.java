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

public class CategoryAdapter extends ListAdapter<CategoryEntity, CategoryAdapter.CategoryViewHolder> {

    private OnCategorySelectedListener listener;
    private long selectedCategoryId = -1;

    public interface OnCategorySelectedListener {
        void onCategorySelected(CategoryEntity category);
    }

    public CategoryAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.listener = listener;
    }

    public void setSelectedCategoryId(long categoryId) {
        long oldSelected = this.selectedCategoryId;
        this.selectedCategoryId = categoryId;

        // Notify changed items
        for (int i = 0; i < getCurrentList().size(); i++) {
            CategoryEntity cat = getItem(i);
            if (cat.getId() == oldSelected || cat.getId() == categoryId) {
                notifyItemChanged(i);
            }
        }
    }

    private static final DiffUtil.ItemCallback<CategoryEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<CategoryEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull CategoryEntity oldItem, @NonNull CategoryEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull CategoryEntity oldItem, @NonNull CategoryEntity newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getIcon().equals(newItem.getIcon());
        }
    };

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryEntity category = getItem(position);
        boolean isSelected = category.getId() == selectedCategoryId;
        holder.bind(category, isSelected, listener, this);
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvIcon;
        private final TextView tvName;
        private final FrameLayout flIconBg;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tv_icon);
            tvName = itemView.findViewById(R.id.tv_name);
            flIconBg = itemView.findViewById(R.id.fl_icon_bg);
        }

        public void bind(CategoryEntity category, boolean isSelected,
                OnCategorySelectedListener listener, CategoryAdapter adapter) {
            tvIcon.setText(category.getIcon());
            tvName.setText(category.getName());

            // Set background color
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);

            try {
                int color = Color.parseColor(category.getColor());
                if (isSelected) {
                    drawable.setColor(color);
                    drawable.setStroke(4, color);
                } else {
                    drawable.setColor(color & 0x33FFFFFF | 0x33000000); // 20% opacity
                }
            } catch (Exception e) {
                drawable.setColor(Color.parseColor("#E0E0E0"));
            }

            flIconBg.setBackground(drawable);

            // Text color based on selection
            tvName.setTextColor(isSelected ? Color.parseColor("#1E3A5F") : Color.parseColor("#6C757D"));

            itemView.setOnClickListener(v -> {
                adapter.setSelectedCategoryId(category.getId());
                if (listener != null) {
                    listener.onCategorySelected(category);
                }
            });
        }
    }
}
