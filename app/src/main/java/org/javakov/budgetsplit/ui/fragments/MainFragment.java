package org.javakov.budgetsplit.ui.fragments;

import android.animation.ValueAnimator;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.javakov.budgetsplit.MainActivity;
import org.javakov.budgetsplit.R;
import org.javakov.budgetsplit.database.entities.Expense;
import org.javakov.budgetsplit.database.entities.Income;
import org.javakov.budgetsplit.database.entities.MoneySource;
import org.javakov.budgetsplit.ui.adapters.MoneySourceAdapter;
import org.javakov.budgetsplit.ui.adapters.RecentTransactionAdapter;
import org.javakov.budgetsplit.utils.CurrencyFormatter;
import org.javakov.budgetsplit.viewmodel.MainViewModel;
import org.javakov.budgetsplit.viewmodel.SettingsViewModel;
import org.javakov.budgetsplit.api.CurrencyExchangeService;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainFragment extends Fragment {

    private MainViewModel mainViewModel;
    private TextView tvTotalBalance;
    private TextView tvConversionRate;
    private MaterialButton btnConvertToUsd;
    private TextView tvNecessitiesAmount;
    private TextView tvNecessitiesPercentage;
    private TextView tvWantsAmount;
    private TextView tvWantsPercentage;
    private TextView tvSavingsAmount;
    private TextView tvSavingsPercentage;
    private TextView tvSourcesCount;
    private TextView tvBudgetHealth;
    private PieChart pieChart;
    private MaterialButton btnAddMoneySource;
    private MaterialButton btnQuickIncome;
    private MaterialButton btnQuickExpense;
    private MaterialButton btnViewAllHistory;
    private RecyclerView recyclerMoneySources;
    private RecyclerView recyclerRecentTransactions;
    private TextView tvEmptyMoneySources;
    private TextView tvEmptyRecentActivity;
    private MoneySourceAdapter moneySourceAdapter;
    private RecentTransactionAdapter recentTransactionAdapter;

    private String currentCurrency = "RUB";
    private double previousBalance = 0.0;
    private boolean isShowingUsdConversion = false;
    private double originalBalance = 0.0;

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
        tvConversionRate = view.findViewById(R.id.tv_conversion_rate);
        btnConvertToUsd = view.findViewById(R.id.btn_convert_to_usd);
        tvNecessitiesAmount = view.findViewById(R.id.tv_necessities_amount);
        tvNecessitiesPercentage = view.findViewById(R.id.tv_necessities_percentage);
        tvWantsAmount = view.findViewById(R.id.tv_wants_amount);
        tvWantsPercentage = view.findViewById(R.id.tv_wants_percentage);
        tvSavingsAmount = view.findViewById(R.id.tv_savings_amount);
        tvSavingsPercentage = view.findViewById(R.id.tv_savings_percentage);
        tvSourcesCount = view.findViewById(R.id.tv_sources_count);
        tvBudgetHealth = view.findViewById(R.id.tv_budget_health);
        tvBudgetHealth.setOnClickListener(v -> showBudgetHealthInfo());
        pieChart = view.findViewById(R.id.pie_chart);
        btnAddMoneySource = view.findViewById(R.id.btn_add_money_source);
        btnQuickIncome = view.findViewById(R.id.btn_quick_income);
        btnQuickExpense = view.findViewById(R.id.btn_quick_expense);
        btnViewAllHistory = view.findViewById(R.id.btn_view_all_history);
        recyclerMoneySources = view.findViewById(R.id.recycler_money_sources);
        recyclerRecentTransactions = view.findViewById(R.id.recycler_recent_transactions);
        tvEmptyMoneySources = view.findViewById(R.id.tv_empty_money_sources);
        tvEmptyRecentActivity = view.findViewById(R.id.tv_empty_recent_activity);
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
        
        // Setup recent transactions recycler
        recentTransactionAdapter = new RecentTransactionAdapter();
        recyclerRecentTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerRecentTransactions.setAdapter(recentTransactionAdapter);
        recyclerRecentTransactions.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        btnAddMoneySource.setOnClickListener(v -> showAddMoneySourceDialog());
        btnConvertToUsd.setOnClickListener(v -> handleCurrencyConversion());
        btnQuickIncome.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showAddIncomeDialog();
            }
        });
        btnQuickExpense.setOnClickListener(v -> showAddExpenseDialog());
        btnViewAllHistory.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToHistory();
            }
        });
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

        mainViewModel.getTotalBalance().observe(getViewLifecycleOwner(), totalBalance -> {
            double currentBalance = Objects.requireNonNullElse(totalBalance, 0.0);
            animateBalanceChange(currentBalance);
        });

        mainViewModel.getTotalNecessities().observe(getViewLifecycleOwner(), necessities -> {
            double amount = Objects.requireNonNullElse(necessities, 0.0);
            tvNecessitiesAmount.setText(CurrencyFormatter.formatCurrency(amount, currentCurrency));
            updateCategoryPercentages();
            updatePieChart();
        });

        mainViewModel.getTotalWants().observe(getViewLifecycleOwner(), wants -> {
            double amount = Objects.requireNonNullElse(wants, 0.0);
            tvWantsAmount.setText(CurrencyFormatter.formatCurrency(amount, currentCurrency));
            updateCategoryPercentages();
            updatePieChart();
        });

        mainViewModel.getTotalSavings().observe(getViewLifecycleOwner(), savings -> {
            double amount = Objects.requireNonNullElse(savings, 0.0);
            tvSavingsAmount.setText(CurrencyFormatter.formatCurrency(amount, currentCurrency));
            updateCategoryPercentages();
            updatePieChart();
            updateBudgetHealth();
        });

        mainViewModel.getAllMoneySources().observe(getViewLifecycleOwner(), moneySources -> {
            android.util.Log.d("MainFragment", "Money sources updated: " + (moneySources != null ? moneySources.size() : "null"));
            if (moneySources != null) {
                for (int i = 0; i < moneySources.size(); i++) {
                    MoneySource source = moneySources.get(i);
                    android.util.Log.d("MainFragment", "Source " + i + ": " + source.getName() + " - " + source.getAmount() + " " + source.getCurrency());
                }
            }
            
            if (moneySources != null && !moneySources.isEmpty()) {
                moneySourceAdapter.submitList(new ArrayList<>(moneySources), () -> recyclerMoneySources.post(() -> {
                    recyclerMoneySources.requestLayout();
                    moneySourceAdapter.notifyDataSetChanged();
                }));
                recyclerMoneySources.setVisibility(View.VISIBLE);
                tvEmptyMoneySources.setVisibility(View.GONE);
                updateSourcesCount(moneySources.size());
            } else {
                moneySourceAdapter.submitList(null);
                recyclerMoneySources.setVisibility(View.GONE);
                tvEmptyMoneySources.setVisibility(View.VISIBLE);
                updateSourcesCount(0);
            }
        });

        // Observe recent transactions with MediatorLiveData to combine both sources
        MediatorLiveData<Boolean> transactionsUpdated = new MediatorLiveData<>();
        transactionsUpdated.addSource(mainViewModel.getAllIncomes(), incomes ->
                transactionsUpdated.setValue(true));
        transactionsUpdated.addSource(mainViewModel.getAllExpenses(), expenses ->
                transactionsUpdated.setValue(true));
        transactionsUpdated.observe(getViewLifecycleOwner(), updated -> {
            if (updated) {
                // Add small delay to ensure both data sources are loaded
                getView().postDelayed(this::updateRecentTransactions, 100);
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
        MaterialAutoCompleteTextView spinnerCurrency = dialogView.findViewById(R.id.spinner_currency);
        MaterialCheckBox cbIsSavings = dialogView.findViewById(R.id.cb_is_savings);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnAdd = dialogView.findViewById(R.id.btn_add);

        // Setup currency dropdown
        String[] currencies = {"RUB (Russian Ruble)", "USD (US Dollar)"};
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_dropdown_item_1line, currencies);
        spinnerCurrency.setAdapter(currencyAdapter);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnAdd.setOnClickListener(v -> {
            String sourceName = etSourceName.getText().toString().trim();
            String amountStr = etSourceAmount.getText().toString().trim();
            String selectedCurrencyText = spinnerCurrency.getText().toString().trim();
            boolean isSavings = cbIsSavings.isChecked();
            
            // Extract currency code from selection (e.g., "RUB" from "RUB (Russian Ruble)")
            String currency = "RUB"; // default
            if (selectedCurrencyText.startsWith("USD")) {
                currency = "USD";
            } else if (selectedCurrencyText.startsWith("RUB")) {
                currency = "RUB";
            }
            
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
                
                mainViewModel.addMoneySource(sourceName, amount, isSavings, currency);
                dialog.dismiss();
                String message = isSavings ? "Savings source added successfully" : "Money source added successfully";
                Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show();
                
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
        MaterialAutoCompleteTextView spinnerCurrency = dialogView.findViewById(R.id.spinner_currency);
        MaterialCheckBox cbIsSavings = dialogView.findViewById(R.id.cb_is_savings);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);

        // Setup currency dropdown
        String[] currencies = {"RUB (Russian Ruble)", "USD (US Dollar)"};
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_dropdown_item_1line, currencies);
        spinnerCurrency.setAdapter(currencyAdapter);

        // Pre-fill with current values
        etSourceName.setText(moneySource.getName());
        etSourceAmount.setText(String.valueOf(moneySource.getAmount()));
        cbIsSavings.setChecked(moneySource.isSavings());
        
        // Set current currency selection
        String currentCurrencyDisplay = moneySource.getCurrency().equals("USD") ? 
            "USD (US Dollar)" : "RUB (Russian Ruble)";
        spinnerCurrency.setText(currentCurrencyDisplay, false);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnSave.setOnClickListener(v -> {
            String sourceName = etSourceName.getText().toString().trim();
            String amountStr = etSourceAmount.getText().toString().trim();
            String selectedCurrencyText = spinnerCurrency.getText().toString().trim();
            boolean isSavings = cbIsSavings.isChecked();
            
            // Extract currency code from selection
            String currency = "RUB"; // default
            if (selectedCurrencyText.startsWith("USD")) {
                currency = "USD";
            } else if (selectedCurrencyText.startsWith("RUB")) {
                currency = "RUB";
            }
            
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
                moneySource.setSavings(isSavings);
                moneySource.setCurrency(currency);
                mainViewModel.updateMoneySource(moneySource);
                
                dialog.dismiss();
                String message = isSavings ? "Savings source updated successfully" : "Money source updated successfully";
                Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show();
                
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
        if (item.getItemId() == R.id.action_settings) {
            showSettingsDialog();
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
                
                // Auto-select the first money source
                if (sourceNames.length > 0) {
                    spinnerMoneySource.setText(sourceNames[0], false);
                }
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
        
        // Update percentages and health indicators
        updateCategoryPercentages();
        updateBudgetHealth();
        
        // Update adapters with current currency
        recentTransactionAdapter.setCurrency(currentCurrency);
    }

    private void animateBalanceChange(double newBalance) {
        ValueAnimator animator = ValueAnimator.ofFloat((float) previousBalance, (float) newBalance);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            tvTotalBalance.setText(CurrencyFormatter.formatCurrency(animatedValue, currentCurrency));
        });
        animator.start();
        previousBalance = newBalance;
    }


    private void updateCategoryPercentages() {
        Double necessities = mainViewModel.getTotalNecessities().getValue();
        Double wants = mainViewModel.getTotalWants().getValue();
        Double savings = mainViewModel.getTotalSavings().getValue();
        Double totalBalance = mainViewModel.getTotalBalance().getValue();

        if (necessities == null) necessities = 0.0;
        if (wants == null) wants = 0.0;
        if (savings == null) savings = 0.0;
        if (totalBalance == null) totalBalance = 0.0;

        if (totalBalance > 0) {
            double necessitiesPercent = (necessities / totalBalance) * 100;
            double wantsPercent = (wants / totalBalance) * 100;
            double savingsPercent = (savings / totalBalance) * 100;

            tvNecessitiesPercentage.setText(String.format("%.0f%%", necessitiesPercent));
            tvWantsPercentage.setText(String.format("%.0f%%", wantsPercent));
            tvSavingsPercentage.setText(String.format("%.0f%%", savingsPercent));
        } else {
            tvNecessitiesPercentage.setText("0%");
            tvWantsPercentage.setText("0%");
            tvSavingsPercentage.setText("0%");
        }
    }

    private void updateSourcesCount(int count) {
        if (count == 0) {
            tvSourcesCount.setText("No sources");
        } else if (count == 1) {
            tvSourcesCount.setText("1 source");
        } else {
            tvSourcesCount.setText(count + " sources");
        }
    }

    private void updateBudgetHealth() {
        Double necessities = mainViewModel.getTotalNecessities().getValue();
        Double wants = mainViewModel.getTotalWants().getValue();
        Double savings = mainViewModel.getTotalSavings().getValue();
        Double totalBalance = mainViewModel.getTotalBalance().getValue();

        if (necessities == null) necessities = 0.0;
        if (wants == null) wants = 0.0;
        if (savings == null) savings = 0.0;
        if (totalBalance == null || totalBalance == 0) {
            tvBudgetHealth.setText(getString(R.string.budget_health_healthy));
            tvBudgetHealth.setBackgroundResource(R.drawable.rounded_background);
            return;
        }

        double savingsPercent = (savings / totalBalance) * 100;
        double necessitiesPercent = (necessities / totalBalance) * 100;

        if (savingsPercent >= 20 && necessitiesPercent <= 60) {
            tvBudgetHealth.setText(getString(R.string.budget_health_healthy));
            tvBudgetHealth.setBackgroundColor(getResources().getColor(R.color.chart_savings, null));
        } else if (savingsPercent >= 10 && necessitiesPercent <= 70) {
            tvBudgetHealth.setText(getString(R.string.budget_health_warning));
            tvBudgetHealth.setBackgroundColor(getResources().getColor(R.color.chart_wants, null));
        } else {
            tvBudgetHealth.setText(getString(R.string.budget_health_critical));
            tvBudgetHealth.setBackgroundColor(getResources().getColor(R.color.md_theme_light_error, null));
        }
    }

    private void showSettingsDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_settings, null);
        
        EditText etNecessitiesPercentage = dialogView.findViewById(R.id.et_necessities_percentage);
        EditText etWantsPercentage = dialogView.findViewById(R.id.et_wants_percentage);
        EditText etSavingsPercentage = dialogView.findViewById(R.id.et_savings_percentage);
        MaterialAutoCompleteTextView spinnerCurrency = dialogView.findViewById(R.id.spinner_currency);
        TextView tvTotalPercentage = dialogView.findViewById(R.id.tv_total_percentage);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);

        // Setup currency spinner
        String[] currencies = {"RUB (Russian Ruble)", "USD (US Dollar)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_dropdown_item_1line, currencies);
        spinnerCurrency.setAdapter(adapter);

        // Create SettingsViewModel
        SettingsViewModel settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        // Text watchers for percentage calculation
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateTotalPercentageInDialog(etNecessitiesPercentage, etWantsPercentage, etSavingsPercentage, tvTotalPercentage);
            }
        };

        etNecessitiesPercentage.addTextChangedListener(textWatcher);
        etWantsPercentage.addTextChangedListener(textWatcher);
        etSavingsPercentage.addTextChangedListener(textWatcher);

        // Load current settings
        settingsViewModel.getBudgetSettings().observe(getViewLifecycleOwner(), settings -> {
            if (settings != null) {
                etNecessitiesPercentage.setText(String.valueOf(settings.getNecessitiesPercentage()));
                etWantsPercentage.setText(String.valueOf(settings.getWantsPercentage()));
                etSavingsPercentage.setText(String.valueOf(settings.getSavingsPercentage()));
                
                String currency = settings.getCurrency();
                if ("RUB".equals(currency)) {
                    spinnerCurrency.setText("RUB (Russian Ruble)", false);
                } else if ("USD".equals(currency)) {
                    spinnerCurrency.setText("USD (US Dollar)", false);
                } else {
                    spinnerCurrency.setText("RUB (Russian Ruble)", false);
                }
                
                updateTotalPercentageInDialog(etNecessitiesPercentage, etWantsPercentage, etSavingsPercentage, tvTotalPercentage);
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnSave.setOnClickListener(v -> {
            try {
                double necessities = getPercentageFromEditTextInDialog(etNecessitiesPercentage);
                double wants = getPercentageFromEditTextInDialog(etWantsPercentage);
                double savings = getPercentageFromEditTextInDialog(etSavingsPercentage);

                double total = necessities + wants + savings;

                if (Math.abs(total - 100.0) >= 0.1) {
                    Snackbar.make(requireView(), getString(R.string.percentage_total_error), 
                        Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (necessities < 0 || wants < 0 || savings < 0) {
                    Snackbar.make(requireView(), "Percentages cannot be negative", 
                        Snackbar.LENGTH_SHORT).show();
                    return;
                }

                String selectedCurrency = spinnerCurrency.getText().toString();
                String currencyCode = "RUB";
                if (selectedCurrency.startsWith("USD")) {
                    currencyCode = "USD";
                } else if (selectedCurrency.startsWith("RUB")) {
                    currencyCode = "RUB";
                }
                
                settingsViewModel.updatePercentages(necessities, wants, savings);
                settingsViewModel.updateCurrency(currencyCode);
                
                dialog.dismiss();
                Snackbar.make(requireView(), getString(R.string.settings_saved), 
                    Snackbar.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {
                Snackbar.make(requireView(), "Please enter valid numbers", 
                    Snackbar.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void updateTotalPercentageInDialog(EditText etNecessities, EditText etWants, EditText etSavings, TextView tvTotal) {
        try {
            double necessities = getPercentageFromEditTextInDialog(etNecessities);
            double wants = getPercentageFromEditTextInDialog(etWants);
            double savings = getPercentageFromEditTextInDialog(etSavings);

            double total = necessities + wants + savings;
            tvTotal.setText(String.format("%.1f%%", total));

            if (Math.abs(total - 100.0) < 0.1) {
                tvTotal.setTextColor(getResources().getColor(R.color.md_theme_light_primary, null));
            } else {
                tvTotal.setTextColor(getResources().getColor(R.color.md_theme_light_error, null));
            }
        } catch (NumberFormatException e) {
            tvTotal.setText("0.0%");
            tvTotal.setTextColor(getResources().getColor(R.color.md_theme_light_error, null));
        }
    }

    private double getPercentageFromEditTextInDialog(EditText editText) {
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return 0.0;
        }
        return Double.parseDouble(text);
    }

    private void updateRecentTransactions() {
        List<Income> incomes = mainViewModel.getAllIncomes().getValue();
        List<Expense> expenses = mainViewModel.getAllExpenses().getValue();
        
        List<RecentTransactionAdapter.TransactionItem> transactions = new ArrayList<>();
        
        // Add incomes
        if (incomes != null) {
            for (Income income : incomes) {
                transactions.add(new RecentTransactionAdapter.TransactionItem(
                    income.getDescription() != null ? income.getDescription() : "Income",
                    income.getAmount(),
                    true,
                    income.getDate()
                ));
            }
        }
        
        // Add expenses  
        if (expenses != null) {
            for (Expense expense : expenses) {
                transactions.add(new RecentTransactionAdapter.TransactionItem(
                    expense.getDescription() != null ? expense.getDescription() : "Expense",
                    expense.getAmount(),
                    false,
                    expense.getDate()
                ));
            }
        }
        
        // Sort by timestamp descending and take only first 3
        transactions.sort((t1, t2) -> Long.compare(t2.timestamp, t1.timestamp));
        
        // Take only the most recent 3 transactions
        List<RecentTransactionAdapter.TransactionItem> recentTransactions = new ArrayList<>();
        int count = Math.min(transactions.size(), 3);
        for (int i = 0; i < count; i++) {
            recentTransactions.add(transactions.get(i));
        }
        
        // Update adapter
        recentTransactionAdapter.setCurrency(currentCurrency);
        recentTransactionAdapter.submitList(new ArrayList<>(recentTransactions));
        
        // Force notify data changed after submitList
        recyclerRecentTransactions.post(() -> recentTransactionAdapter.notifyDataSetChanged());
        
        // Debug: Log the number of transactions being submitted
        android.util.Log.d("MainFragment", "Submitting " + recentTransactions.size() + " recent transactions");
        
        // Show/hide empty state
        if (recentTransactions.isEmpty()) {
            recyclerRecentTransactions.setVisibility(View.GONE);
            tvEmptyRecentActivity.setVisibility(View.VISIBLE);
        } else {
            recyclerRecentTransactions.setVisibility(View.VISIBLE);
            tvEmptyRecentActivity.setVisibility(View.GONE);
        }
    }

    private void showBudgetHealthInfo() {
        String title = "Budget Status";
        String message = "The status shows the health of your budget:\n\n" +
                "🟢 Healthy - Savings ≥ 20% and Necessities ≤ 60%\n" +
                "🟡 Warning - Savings ≥ 10% and Necessities ≤ 70%\n" +
                "🔴 Critical - Too little savings or too many expenses\n\n" +
                "Recommendations:\n" +
                "• Aim to save at least 20% of income\n" +
                "• Keep essential expenses within 50-60%\n" +
                "• Use remaining 20-30% for entertainment";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Got it", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void handleCurrencyConversion() {
        if (isShowingUsdConversion) {
            // Return to original currency
            showOriginalCurrency();
        } else {
            // Convert to USD
            convertToUsd();
        }
    }

    private void convertToUsd() {
        if (currentCurrency.equals("USD")) {
            Snackbar.make(requireView(), "Already showing USD", Snackbar.LENGTH_SHORT).show();
            return;
        }

        Double totalBalance = mainViewModel.getTotalBalance().getValue();
        if (totalBalance == null || totalBalance == 0) {
            Snackbar.make(requireView(), "No balance to convert", Snackbar.LENGTH_SHORT).show();
            return;
        }

        originalBalance = totalBalance;
        btnConvertToUsd.setEnabled(false);
        
        CurrencyExchangeService.getExchangeRate(currentCurrency, "USD")
            .thenAccept(exchangeRate -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        double convertedAmount = CurrencyExchangeService.convertAmount(originalBalance, exchangeRate.rate);
                        
                        // Show conversion with animation
                        animateBalanceConversion(originalBalance, convertedAmount, "USD");
                        
                        // Show conversion rate (reverse: how many RUB per 1 USD)
                        double rubPerUsd = 1.0 / exchangeRate.rate;
                        String rateText = String.format("1 USD = %.2f %s", rubPerUsd, currentCurrency);
                        tvConversionRate.setText(rateText);
                        tvConversionRate.setVisibility(View.VISIBLE);
                        
                        isShowingUsdConversion = true;
                        btnConvertToUsd.setText(getString(R.string.convert_to_rub));
                        btnConvertToUsd.setEnabled(true);
                        
                        // Auto-revert after 5 seconds
                        tvTotalBalance.postDelayed(this::showOriginalCurrency, 5000);
                    });
                }
            })
            .exceptionally(throwable -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnConvertToUsd.setEnabled(true);
                        Snackbar.make(requireView(), "Failed to get exchange rate: " + throwable.getMessage(), 
                                Snackbar.LENGTH_LONG).show();
                    });
                }
                return null;
            });
    }

    private void showOriginalCurrency() {
        if (!isShowingUsdConversion) return;
        
        Double currentUsdAmount = parseBalanceAmount(tvTotalBalance.getText().toString());
        animateBalanceConversion(currentUsdAmount != null ? currentUsdAmount : 0, originalBalance, currentCurrency);
        
        tvConversionRate.setVisibility(View.GONE);
        isShowingUsdConversion = false;
        btnConvertToUsd.setText(getString(R.string.convert_to_usd));
    }

    private void animateBalanceConversion(double fromAmount, double toAmount, String toCurrency) {
        ValueAnimator animator = ValueAnimator.ofFloat((float) fromAmount, (float) toAmount);
        animator.setDuration(800);
        animator.setInterpolator(new DecelerateInterpolator());
        
        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            tvTotalBalance.setText(CurrencyFormatter.formatCurrency(animatedValue, toCurrency));
        });
        
        animator.start();
    }

    private Double parseBalanceAmount(String balanceText) {
        if (balanceText == null || balanceText.isEmpty()) {
            return null;
        }
        
        try {
            // Remove currency symbols and spaces
            String cleanText = balanceText.replaceAll("[^0-9.,-]", "").trim();
            
            // Handle different decimal separators
            if (cleanText.contains(",") && cleanText.contains(".")) {
                // Format like 1,234.56
                cleanText = cleanText.replace(",", "");
            } else if (cleanText.contains(",")) {
                // Format like 1234,56 (European style)
                cleanText = cleanText.replace(",", ".");
            }
            
            return Double.parseDouble(cleanText);
        } catch (NumberFormatException e) {
            android.util.Log.e("MainFragment", "Failed to parse balance amount: " + balanceText, e);
            return null;
        }
    }
}