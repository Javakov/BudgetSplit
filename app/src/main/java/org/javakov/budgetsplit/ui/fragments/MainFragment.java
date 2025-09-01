package org.javakov.budgetsplit.ui.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.javakov.budgetsplit.MainActivity;
import org.javakov.budgetsplit.R;
import org.javakov.budgetsplit.database.entities.MoneySource;
import org.javakov.budgetsplit.ui.adapters.MoneySourceAdapter;
import org.javakov.budgetsplit.utils.CurrencyFormatter;
import org.javakov.budgetsplit.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainFragment extends Fragment {

    private MainViewModel mainViewModel;
    private TextView tvTotalBalance;
    private TextView tvNecessitiesAmount;
    private TextView tvWantsAmount;
    private TextView tvSavingsAmount;
    private PieChart pieChart;
    private MaterialButton btnAddMoneySource;
    private RecyclerView recyclerMoneySources;
    private TextView tvEmptyMoneySources;
    private MoneySourceAdapter moneySourceAdapter;

    private String currentCurrency = "RUB";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupPieChart();
        setupRecyclerView();
        observeData();
        setupClickListeners();
    }

    private void initializeViews(View view) {
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvNecessitiesAmount = view.findViewById(R.id.tv_necessities_amount);
        tvWantsAmount = view.findViewById(R.id.tv_wants_amount);
        tvSavingsAmount = view.findViewById(R.id.tv_savings_amount);
        pieChart = view.findViewById(R.id.pie_chart);
        btnAddMoneySource = view.findViewById(R.id.btn_add_money_source);
        recyclerMoneySources = view.findViewById(R.id.recycler_money_sources);
        tvEmptyMoneySources = view.findViewById(R.id.tv_empty_money_sources);
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);
        
        pieChart.getLegend().setEnabled(false);
    }

    private void setupRecyclerView() {
        moneySourceAdapter = new MoneySourceAdapter(
            // Delete listener
            moneySource -> {
                mainViewModel.deleteMoneySource(moneySource);
                Snackbar.make(requireView(), "Money source deleted", Snackbar.LENGTH_SHORT).show();
            },
            // Edit listener
                this::showEditMoneySourceDialog
        );
        
        recyclerMoneySources.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerMoneySources.setAdapter(moneySourceAdapter);
    }

    private void setupClickListeners() {
        btnAddMoneySource.setOnClickListener(v -> showAddMoneySourceDialog());
    }

    private void observeData() {
        mainViewModel.getBudgetSettings().observe(getViewLifecycleOwner(), settings -> {
            if (settings != null) {
                currentCurrency = settings.getCurrency();
                if (currentCurrency == null) {
                    currentCurrency = "RUB";
                }
                updateAllCurrencyDisplays();
            }
        });

        mainViewModel.getTotalBalance().observe(getViewLifecycleOwner(), totalBalance ->
                tvTotalBalance.setText(CurrencyFormatter.formatCurrency(
                        Objects.requireNonNullElse(totalBalance, 0.0), currentCurrency)));

        mainViewModel.getTotalNecessities().observe(getViewLifecycleOwner(), necessities -> {
            tvNecessitiesAmount.setText(CurrencyFormatter.formatCurrency(
                    Objects.requireNonNullElse(necessities, 0.0), currentCurrency));
            updatePieChart();
        });

        mainViewModel.getTotalWants().observe(getViewLifecycleOwner(), wants -> {
            tvWantsAmount.setText(CurrencyFormatter.formatCurrency(
                    Objects.requireNonNullElse(wants, 0.0), currentCurrency));
            updatePieChart();
        });

        mainViewModel.getTotalSavings().observe(getViewLifecycleOwner(), savings -> {
            tvSavingsAmount.setText(CurrencyFormatter.formatCurrency(
                    Objects.requireNonNullElse(savings, 0.0), currentCurrency));
            updatePieChart();
        });

        mainViewModel.getAllMoneySources().observe(getViewLifecycleOwner(), moneySources -> {
            if (moneySources != null && !moneySources.isEmpty()) {
                moneySourceAdapter.submitList(moneySources);
                recyclerMoneySources.setVisibility(View.VISIBLE);
                tvEmptyMoneySources.setVisibility(View.GONE);
            } else {
                moneySourceAdapter.submitList(moneySources);
                recyclerMoneySources.setVisibility(View.GONE);
                tvEmptyMoneySources.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updatePieChart() {
        Double necessities = mainViewModel.getTotalNecessities().getValue();
        Double wants = mainViewModel.getTotalWants().getValue();
        Double savings = mainViewModel.getTotalSavings().getValue();

        if (necessities == null) necessities = 0.0;
        if (wants == null) wants = 0.0;
        if (savings == null) savings = 0.0;

        double total = necessities + wants + savings;

        if (total == 0) {
            pieChart.setVisibility(View.GONE);
            return;
        }

        pieChart.setVisibility(View.VISIBLE);

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (necessities > 0) {
            entries.add(new PieEntry(necessities.floatValue(), getString(R.string.necessities)));
            colors.add(getResources().getColor(R.color.chart_necessities, null));
        }
        if (wants > 0) {
            entries.add(new PieEntry(wants.floatValue(), getString(R.string.wants)));
            colors.add(getResources().getColor(R.color.chart_wants, null));
        }
        if (savings > 0) {
            entries.add(new PieEntry(savings.floatValue(), getString(R.string.savings)));
            colors.add(getResources().getColor(R.color.chart_savings, null));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    private void showAddMoneySourceDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_money_source, null);
        
        EditText etSourceName = dialogView.findViewById(R.id.et_source_name);
        EditText etSourceAmount = dialogView.findViewById(R.id.et_source_amount);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnAdd = dialogView.findViewById(R.id.btn_add);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnAdd.setOnClickListener(v -> {
            String sourceName = etSourceName.getText().toString().trim();
            String amountStr = etSourceAmount.getText().toString().trim();
            
            if (TextUtils.isEmpty(sourceName)) {
                Snackbar.make(requireView(), "Please enter a source name", Snackbar.LENGTH_SHORT).show();
                return;
            }
            
            if (TextUtils.isEmpty(amountStr)) {
                Snackbar.make(requireView(), getString(R.string.invalid_amount), Snackbar.LENGTH_SHORT).show();
                return;
            }
            
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Snackbar.make(requireView(), getString(R.string.invalid_amount), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                
                mainViewModel.addMoneySource(sourceName, amount);
                dialog.dismiss();
                Snackbar.make(requireView(), "Money source added successfully", Snackbar.LENGTH_SHORT).show();
                
            } catch (NumberFormatException e) {
                Snackbar.make(requireView(), getString(R.string.invalid_amount), Snackbar.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showEditMoneySourceDialog(MoneySource moneySource) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_money_source, null);
        
        EditText etSourceName = dialogView.findViewById(R.id.et_source_name);
        EditText etSourceAmount = dialogView.findViewById(R.id.et_source_amount);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);

        // Pre-fill with current values
        etSourceName.setText(moneySource.getName());
        etSourceAmount.setText(String.valueOf(moneySource.getAmount()));

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnSave.setOnClickListener(v -> {
            String sourceName = etSourceName.getText().toString().trim();
            String amountStr = etSourceAmount.getText().toString().trim();
            
            if (TextUtils.isEmpty(sourceName)) {
                Snackbar.make(requireView(), "Please enter a source name", Snackbar.LENGTH_SHORT).show();
                return;
            }
            
            if (TextUtils.isEmpty(amountStr)) {
                Snackbar.make(requireView(), getString(R.string.invalid_amount), Snackbar.LENGTH_SHORT).show();
                return;
            }
            
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Snackbar.make(requireView(), getString(R.string.invalid_amount), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                
                // Update the money source
                moneySource.setName(sourceName);
                moneySource.setAmount(amount);
                mainViewModel.updateMoneySource(moneySource);
                
                dialog.dismiss();
                Snackbar.make(requireView(), "Money source updated successfully", Snackbar.LENGTH_SHORT).show();
                
            } catch (NumberFormatException e) {
                Snackbar.make(requireView(), getString(R.string.invalid_amount), Snackbar.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_income) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showAddIncomeDialog();
            }
            return true;
        } else if (item.getItemId() == R.id.action_add_expense) {
            showAddExpenseDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddExpenseDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_expense, null);
        
        EditText etAmount = dialogView.findViewById(R.id.et_amount);
        EditText etDescription = dialogView.findViewById(R.id.et_description);
        MaterialAutoCompleteTextView spinnerMoneySource = dialogView.findViewById(R.id.spinner_money_source);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnAdd = dialogView.findViewById(R.id.btn_add);

        // Setup money source dropdown
        mainViewModel.getAllMoneySources().observe(getViewLifecycleOwner(), moneySources -> {
            if (moneySources != null && !moneySources.isEmpty()) {
                String[] sourceNames = new String[moneySources.size()];
                for (int i = 0; i < moneySources.size(); i++) {
                    sourceNames[i] = moneySources.get(i).getName();
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                    android.R.layout.simple_dropdown_item_1line, sourceNames);
                spinnerMoneySource.setAdapter(adapter);
            }
        });

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnAdd.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String selectedSource = spinnerMoneySource.getText().toString().trim();
            
            if (TextUtils.isEmpty(amountStr)) {
                Snackbar.make(requireView(), getString(R.string.invalid_amount), Snackbar.LENGTH_SHORT).show();
                return;
            }
            
            if (TextUtils.isEmpty(selectedSource)) {
                Snackbar.make(requireView(), "Please select a money source", Snackbar.LENGTH_SHORT).show();
                return;
            }
            
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Snackbar.make(requireView(), getString(R.string.invalid_amount), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                
                if (TextUtils.isEmpty(description)) {
                    description = "Expense";
                }
                
                mainViewModel.addExpenseWithSource(amount, description, selectedSource);
                dialog.dismiss();
                Snackbar.make(requireView(), "Expense added successfully", Snackbar.LENGTH_SHORT).show();
                
            } catch (NumberFormatException e) {
                Snackbar.make(requireView(), getString(R.string.invalid_amount), Snackbar.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void updateAllCurrencyDisplays() {
        Double totalBalance = mainViewModel.getTotalBalance().getValue();
        Double necessities = mainViewModel.getTotalNecessities().getValue();
        Double wants = mainViewModel.getTotalWants().getValue();
        Double savings = mainViewModel.getTotalSavings().getValue();

        if (totalBalance != null) {
            tvTotalBalance.setText(CurrencyFormatter.formatCurrency(totalBalance, currentCurrency));
        }
        if (necessities != null) {
            tvNecessitiesAmount.setText(CurrencyFormatter.formatCurrency(necessities, currentCurrency));
        }
        if (wants != null) {
            tvWantsAmount.setText(CurrencyFormatter.formatCurrency(wants, currentCurrency));
        }
        if (savings != null) {
            tvSavingsAmount.setText(CurrencyFormatter.formatCurrency(savings, currentCurrency));
        }
    }
}