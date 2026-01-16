package com.smartbudget.app.presentation.savings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.smartbudget.app.data.local.entity.SavingsGoalEntity;
import com.smartbudget.app.databinding.ItemSavingsGoalBinding;
import com.smartbudget.app.utils.DateUtils;

import java.text.NumberFormat;
import java.util.Locale;

public class SavingsGoalAdapter extends ListAdapter<SavingsGoalEntity, SavingsGoalAdapter.GoalViewHolder> {

    private OnAddMoneyClickListener onAddMoneyClickListener;

    public SavingsGoalAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<SavingsGoalEntity> DIFF_CALLBACK = 
            new DiffUtil.ItemCallback<SavingsGoalEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull SavingsGoalEntity oldItem, @NonNull SavingsGoalEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull SavingsGoalEntity oldItem, @NonNull SavingsGoalEntity newItem) {
            return oldItem.getCurrentAmount() == newItem.getCurrentAmount() &&
                   oldItem.isCompleted() == newItem.isCompleted();
        }
    };

    public void setOnAddMoneyClickListener(OnAddMoneyClickListener listener) {
        this.onAddMoneyClickListener = listener;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSavingsGoalBinding binding = ItemSavingsGoalBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new GoalViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class GoalViewHolder extends RecyclerView.ViewHolder {
        private final ItemSavingsGoalBinding binding;
        private final NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        GoalViewHolder(ItemSavingsGoalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SavingsGoalEntity goal) {
            binding.tvIcon.setText(goal.getIcon() != null ? goal.getIcon() : "🎯");
            binding.tvName.setText(goal.getName());
            binding.tvDeadline.setText("Hạn: " + DateUtils.formatDate(goal.getDeadline()));
            
            int progress = goal.getProgressPercentage();
            binding.tvPercentage.setText(progress + "%");
            binding.progressBar.setProgress(progress);
            
            binding.tvCurrent.setText("Đã tiết kiệm: " + formatter.format(goal.getCurrentAmount()) + " ₫");
            binding.tvTarget.setText("Mục tiêu: " + formatter.format(goal.getTargetAmount()) + " ₫");
            
            binding.btnAddMoney.setOnClickListener(v -> {
                if (onAddMoneyClickListener != null) {
                    onAddMoneyClickListener.onAddMoneyClick(goal);
                }
            });
            
            // Hide add money button if completed
            if (goal.isCompleted()) {
                binding.btnAddMoney.setVisibility(View.GONE);
            } else {
                binding.btnAddMoney.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface OnAddMoneyClickListener {
        void onAddMoneyClick(SavingsGoalEntity goal);
    }
}
