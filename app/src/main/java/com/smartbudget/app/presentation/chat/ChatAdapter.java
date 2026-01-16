package com.smartbudget.app.presentation.chat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.smartbudget.app.R;
import com.smartbudget.app.data.local.entity.ChatMessageEntity;

/**
 * ListAdapter for chat messages with DiffUtil for efficient updates.
 * Replaces the old RecyclerView.Adapter with notifyDataSetChanged().
 */
public class ChatAdapter extends ListAdapter<ChatMessageEntity, ChatAdapter.MessageViewHolder> {

    /**
     * DiffUtil callback for efficient list updates.
     */
    private static final DiffUtil.ItemCallback<ChatMessageEntity> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<ChatMessageEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull ChatMessageEntity oldItem, @NonNull ChatMessageEntity newItem) {
                // Compare by timestamp since messages are unique by time
                return oldItem.timestamp == newItem.timestamp;
            }

            @Override
            public boolean areContentsTheSame(@NonNull ChatMessageEntity oldItem, @NonNull ChatMessageEntity newItem) {
                return oldItem.content.equals(newItem.content) && 
                       oldItem.role.equals(newItem.role);
            }
        };

    public ChatAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessageEntity message = getItem(position);
        holder.bind(message);
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardMessage;
        private final TextView tvMessage;
        private final TextView tvTime;
        private final LinearLayout container;
        
        private static final java.text.SimpleDateFormat TIME_FORMAT = 
            new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardMessage = itemView.findViewById(R.id.card_message);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            container = itemView.findViewById(R.id.container);
        }

        void bind(ChatMessageEntity message) {
            tvMessage.setText(message.content);
            tvTime.setText(TIME_FORMAT.format(new java.util.Date(message.timestamp)));

            // Style based on sender
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardMessage.getLayoutParams();
            
            boolean isUser = "user".equals(message.role);
            
            if (isUser) {
                // User message - right aligned, primary color
                container.setGravity(Gravity.END);
                params.setMarginStart(64);
                params.setMarginEnd(0);
                cardMessage.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), R.color.primary));
                tvMessage.setTextColor(
                    ContextCompat.getColor(itemView.getContext(), android.R.color.white));
                tvTime.setTextColor(
                    ContextCompat.getColor(itemView.getContext(), android.R.color.white));
                tvTime.setAlpha(0.7f);
            } else {
                // AI message - left aligned, card background
                container.setGravity(Gravity.START);
                params.setMarginStart(0);
                params.setMarginEnd(64);
                cardMessage.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), R.color.card_background));
                tvMessage.setTextColor(
                    ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
                tvTime.setTextColor(
                    ContextCompat.getColor(itemView.getContext(), R.color.text_hint));
                tvTime.setAlpha(1f);
            }
            
            cardMessage.setLayoutParams(params);
        }
    }
}
