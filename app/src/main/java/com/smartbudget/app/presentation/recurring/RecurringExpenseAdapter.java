package com.smartbudget.app.presentation.recurring;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.smartbudget.app.R;
import com.smartbudget.app.data.local.entity.RecurringExpenseEntity;

import java.text.NumberFormat;
import java.util.Locale;

public class RecurringExpenseAdapter extends ListAdapter<RecurringExpenseEntity, RecurringExpenseAdapter.ViewHolder> {

    private OnItemClickListener onItemClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnItemClickListener {
        void onItemClick(RecurringExpenseEntity entity);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(RecurringExpenseEntity entity);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    public RecurringExpenseAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<RecurringExpenseEntity> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<RecurringExpenseEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull RecurringExpenseEntity oldItem, @NonNull RecurringExpenseEntity newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull RecurringExpenseEntity oldItem, @NonNull RecurringExpenseEntity newItem) {
                return oldItem.getName().equals(newItem.getName()) &&
                       oldItem.getAmount() == newItem.getAmount() &&
                       oldItem.isActive() == newItem.isActive();
            }
        };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recurring_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecurringExpenseEntity entity = getItem(position);
        holder.bind(entity);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvAmount;
        private final TextView tvSchedule;
        private final TextView tvStatus;
        private final View statusIndicator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_recurring_name);
            tvAmount = itemView.findViewById(R.id.tv_recurring_amount);
            tvSchedule = itemView.findViewById(R.id.tv_recurring_schedule);
            tvStatus = itemView.findViewById(R.id.tv_recurring_status);
            statusIndicator = itemView.findViewById(R.id.view_status_indicator);
        }

        void bind(RecurringExpenseEntity entity) {
            tvName.setText(entity.getName());
            
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvAmount.setText(formatter.format(entity.getAmount()) + " ₫");
            
            tvSchedule.setText("Ngày " + entity.getDayOfMonth() + " " + entity.getFrequencyText());
            
            if (entity.isActive()) {
                tvStatus.setText(R.string.status_active);
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.success));
                statusIndicator.setBackgroundColor(itemView.getContext().getColor(R.color.success));
            } else {
                tvStatus.setText(R.string.status_paused);
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.text_secondary));
                statusIndicator.setBackgroundColor(itemView.getContext().getColor(R.color.text_secondary));
            }

            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(entity);
                }
            });
        }
    }
}
