package org.javakov.budgetsplit.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.javakov.budgetsplit.R;
import org.javakov.budgetsplit.database.entities.Income;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IncomeHistoryAdapter extends RecyclerView.Adapter<IncomeHistoryAdapter.IncomeViewHolder> {

    private List<Income> incomes = new ArrayList<>();
    private final NumberFormat currencyFormat;
    private final SimpleDateFormat dateFormat;

    public IncomeHistoryAdapter() {
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_income_history, parent, false);
        return new IncomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncomeViewHolder holder, int position) {
        Income income = incomes.get(position);
        holder.bind(income);
    }

    @Override
    public int getItemCount() {
        return incomes.size();
    }

    public void setIncomes(List<Income> incomes) {
        this.incomes = incomes != null ? incomes : new ArrayList<>();
        notifyDataSetChanged();
    }

    class IncomeViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDescription;
        private final TextView tvDate;
        private final TextView tvAmount;
        private final TextView tvNecessitiesAmount;
        private final TextView tvWantsAmount;
        private final TextView tvSavingsAmount;

        public IncomeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvNecessitiesAmount = itemView.findViewById(R.id.tv_necessities_amount);
            tvWantsAmount = itemView.findViewById(R.id.tv_wants_amount);
            tvSavingsAmount = itemView.findViewById(R.id.tv_savings_amount);
        }

        public void bind(Income income) {
            tvDescription.setText(income.getDescription());
            tvDate.setText(dateFormat.format(new Date(income.getDate())));
            tvAmount.setText(currencyFormat.format(income.getAmount()));
            tvNecessitiesAmount.setText(currencyFormat.format(income.getNecessitiesAmount()));
            tvWantsAmount.setText(currencyFormat.format(income.getWantsAmount()));
            tvSavingsAmount.setText(currencyFormat.format(income.getSavingsAmount()));
        }
    }
}