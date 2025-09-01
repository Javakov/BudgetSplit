package org.javakov.budgetsplit.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.javakov.budgetsplit.R;
import org.javakov.budgetsplit.database.entities.Expense;
import org.javakov.budgetsplit.database.entities.Income;
import org.javakov.budgetsplit.utils.CurrencyFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.TransactionViewHolder> {

    private final List<TransactionItem> transactions = new ArrayList<>();
    private String currentCurrency = "RUB";
    private final SimpleDateFormat dateFormat;

    public TransactionHistoryAdapter() {
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    public void setCurrency(String currency) {
        this.currentCurrency = currency;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_history, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionItem transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void setTransactions(List<Income> incomes, List<Expense> expenses) {
        this.transactions.clear();
        
        // Add incomes as transactions
        if (incomes != null) {
            for (Income income : incomes) {
                transactions.add(new TransactionItem(income));
            }
        }
        
        // Add expenses as transactions
        if (expenses != null) {
            for (Expense expense : expenses) {
                transactions.add(new TransactionItem(expense));
            }
        }
        
        // Sort by date (newest first)
        transactions.sort((a, b) -> Long.compare(b.getDate(), a.getDate()));
        
        notifyDataSetChanged();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvType;
        private final TextView tvDescription;
        private final TextView tvDate;
        private final TextView tvAmount;
        private final TextView tvSource;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_type);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvSource = itemView.findViewById(R.id.tv_source);
        }

        public void bind(TransactionItem transaction) {
            tvDescription.setText(transaction.getDescription());
            tvDate.setText(dateFormat.format(new Date(transaction.getDate())));
            tvAmount.setText(CurrencyFormatter.formatCurrency(transaction.getAmount(), currentCurrency));
            
            if (transaction.isIncome()) {
                tvType.setText("Income");
                tvType.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
                tvAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
                if (transaction.getSourceName() != null) {
                    tvSource.setText("To: " + transaction.getSourceName());
                } else {
                    tvSource.setText("Added to balance");
                }
            } else {
                tvType.setText("Expense");
                tvType.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
                tvAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
                tvSource.setText("From: " + transaction.getSourceName());
            }
        }
    }

    // Helper class to unify Income and Expense
    public static class TransactionItem {
        private final String description;
        private final long date;
        private final double amount;
        private final boolean isIncome;
        private final String sourceName;

        public TransactionItem(Income income) {
            this.description = income.getDescription();
            this.date = income.getDate();
            this.amount = income.getAmount();
            this.isIncome = true;
            this.sourceName = income.getSourceName();
        }

        public TransactionItem(Expense expense) {
            this.description = expense.getDescription();
            this.date = expense.getDate();
            this.amount = expense.getAmount();
            this.isIncome = false;
            this.sourceName = expense.getSourceName();
        }

        public String getDescription() { return description; }
        public long getDate() { return date; }
        public double getAmount() { return amount; }
        public boolean isIncome() { return isIncome; }
        public String getSourceName() { return sourceName; }
    }
}