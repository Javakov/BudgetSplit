package org.javakov.budgetsplit.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.javakov.budgetsplit.R;
import org.javakov.budgetsplit.utils.CurrencyFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RecentTransactionAdapter extends ListAdapter<RecentTransactionAdapter.TransactionItem, RecentTransactionAdapter.TransactionViewHolder> {

    private String currency = "RUB";

    public static class TransactionItem {
        public final String description;
        public final double amount;
        public final boolean isIncome;
        public final long timestamp;

        public TransactionItem(String description, double amount, boolean isIncome, long timestamp) {
            this.description = description;
            this.amount = amount;
            this.isIncome = isIncome;
            this.timestamp = timestamp;
        }
    }

    private static final DiffUtil.ItemCallback<TransactionItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull TransactionItem oldItem, @NonNull TransactionItem newItem) {
            return oldItem.timestamp == newItem.timestamp &&
                    oldItem.description.equals(newItem.description) &&
                    oldItem.amount == newItem.amount;
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransactionItem oldItem, @NonNull TransactionItem newItem) {
            return oldItem.description.equals(newItem.description) &&
                    oldItem.amount == newItem.amount &&
                    oldItem.isIncome == newItem.isIncome &&
                    oldItem.timestamp == newItem.timestamp;
        }
    };

    public RecentTransactionAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setCurrency(String currency) {
        this.currency = currency;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionItem item = getItem(position);
        holder.bind(item, currency);
        android.util.Log.d("RecentTransactionAdapter", "Binding item at position " + position + ": " + item.description);
    }

    @Override
    public int getItemCount() {
        int count = super.getItemCount();
        android.util.Log.d("RecentTransactionAdapter", "Item count: " + count);
        return count;
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivTransactionType;
        private final TextView tvTransactionDescription;
        private final TextView tvTransactionDate;
        private final TextView tvTransactionAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTransactionType = itemView.findViewById(R.id.iv_transaction_type);
            tvTransactionDescription = itemView.findViewById(R.id.tv_transaction_description);
            tvTransactionDate = itemView.findViewById(R.id.tv_transaction_date);
            tvTransactionAmount = itemView.findViewById(R.id.tv_transaction_amount);
        }

        public void bind(TransactionItem item, String currency) {
            tvTransactionDescription.setText(item.description);

            // Format date
            String dateText = formatDate(item.timestamp);
            tvTransactionDate.setText(dateText);

            // Format amount with sign
            String amountText;
            int colorRes;
            int iconRes;
            
            if (item.isIncome) {
                amountText = "+" + CurrencyFormatter.formatCurrency(item.amount, currency);
                colorRes = R.color.chart_savings;
                iconRes = android.R.drawable.ic_input_add;
            } else {
                amountText = "-" + CurrencyFormatter.formatCurrency(item.amount, currency);
                colorRes = R.color.md_theme_light_error;
                iconRes = android.R.drawable.ic_menu_revert;
            }

            tvTransactionAmount.setText(amountText);
            tvTransactionAmount.setTextColor(itemView.getContext().getResources().getColor(colorRes, null));
            
            ivTransactionType.setImageResource(iconRes);
            ivTransactionType.setColorFilter(itemView.getContext().getResources().getColor(colorRes, null));
        }

        private String formatDate(long timestamp) {
            Date date = new Date(timestamp);
            Calendar calendar = Calendar.getInstance();
            Calendar today = Calendar.getInstance();
            calendar.setTime(date);

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            
            // Check if it's today
            if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                return "Today " + timeFormat.format(date);
            }
            
            // Check if it's yesterday
            today.add(Calendar.DAY_OF_YEAR, -1);
            if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                return "Yesterday " + timeFormat.format(date);
            }

            // Otherwise show date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());
            return dateFormat.format(date) + " " + timeFormat.format(date);
        }
    }
}