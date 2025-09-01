package org.javakov.budgetsplit.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.javakov.budgetsplit.R;
import org.javakov.budgetsplit.ui.adapters.TransactionHistoryAdapter;
import org.javakov.budgetsplit.viewmodel.HistoryViewModel;

public class HistoryFragment extends Fragment {

    private HistoryViewModel historyViewModel;
    private RecyclerView recyclerView;
    private TransactionHistoryAdapter adapter;
    private TextView tvEmptyState;
    private String currentCurrency = "RUB";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        observeData();
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_history);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
    }

    private void setupRecyclerView() {
        adapter = new TransactionHistoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void observeData() {
        // Observe budget settings for currency changes
        historyViewModel.getBudgetSettings().observe(getViewLifecycleOwner(), settings -> {
            if (settings != null) {
                currentCurrency = settings.getCurrency();
                if (currentCurrency == null) {
                    currentCurrency = "RUB";
                }
                adapter.setCurrency(currentCurrency);
            }
        });

        // Observe both incomes and expenses and combine them
        historyViewModel.getAllIncomes().observe(getViewLifecycleOwner(), incomes ->
                updateTransactions());
        
        historyViewModel.getAllExpenses().observe(getViewLifecycleOwner(), expenses ->
                updateTransactions());
    }
    
    private void updateTransactions() {
        var incomes = historyViewModel.getAllIncomes().getValue();
        var expenses = historyViewModel.getAllExpenses().getValue();
        
        boolean hasTransactions = (incomes != null && !incomes.isEmpty()) || 
                                 (expenses != null && !expenses.isEmpty());
        
        if (hasTransactions) {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            adapter.setTransactions(incomes, expenses);
        } else {
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        }
    }
}