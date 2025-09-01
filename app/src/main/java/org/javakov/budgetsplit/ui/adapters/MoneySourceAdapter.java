package org.javakov.budgetsplit.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import org.javakov.budgetsplit.R;
import org.javakov.budgetsplit.database.entities.MoneySource;

import java.text.NumberFormat;
import java.util.Locale;

public class MoneySourceAdapter extends ListAdapter<MoneySource, MoneySourceAdapter.MoneySourceViewHolder> {

    private final NumberFormat currencyFormat;
    private final OnDeleteClickListener onDeleteClickListener;
    private final OnEditClickListener onEditClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(MoneySource moneySource);
    }

    public interface OnEditClickListener {
        void onEditClick(MoneySource moneySource);
    }

    public MoneySourceAdapter(OnDeleteClickListener onDeleteClickListener, OnEditClickListener onEditClickListener) {
        super(DIFF_CALLBACK);
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        this.onDeleteClickListener = onDeleteClickListener;
        this.onEditClickListener = onEditClickListener;
    }

    private static final DiffUtil.ItemCallback<MoneySource> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<MoneySource>() {
            @Override
            public boolean areItemsTheSame(@NonNull MoneySource oldItem, @NonNull MoneySource newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull MoneySource oldItem, @NonNull MoneySource newItem) {
                return oldItem.getName().equals(newItem.getName()) &&
                       Double.compare(oldItem.getAmount(), newItem.getAmount()) == 0;
            }
        };

    @NonNull
    @Override
    public MoneySourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_money_source, parent, false);
        return new MoneySourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoneySourceViewHolder holder, int position) {
        MoneySource moneySource = getItem(position);
        holder.bind(moneySource);
    }

    class MoneySourceViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSourceName;
        private final TextView tvSourceAmount;
        private final MaterialButton btnEditSource;
        private final MaterialButton btnDeleteSource;

        public MoneySourceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSourceName = itemView.findViewById(R.id.tv_source_name);
            tvSourceAmount = itemView.findViewById(R.id.tv_source_amount);
            btnEditSource = itemView.findViewById(R.id.btn_edit_source);
            btnDeleteSource = itemView.findViewById(R.id.btn_delete_source);
        }

        public void bind(MoneySource moneySource) {
            tvSourceName.setText(moneySource.getName());
            tvSourceAmount.setText(currencyFormat.format(moneySource.getAmount()));
            
            btnEditSource.setOnClickListener(v -> {
                if (onEditClickListener != null) {
                    onEditClickListener.onEditClick(moneySource);
                }
            });
            
            btnDeleteSource.setOnClickListener(v -> {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(moneySource);
                }
            });
        }
    }
}