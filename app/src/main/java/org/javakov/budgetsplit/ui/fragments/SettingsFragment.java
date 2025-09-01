package org.javakov.budgetsplit.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.javakov.budgetsplit.R;
import org.javakov.budgetsplit.database.entities.BudgetSettings;
import org.javakov.budgetsplit.viewmodel.SettingsViewModel;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;
    private EditText etNecessitiesPercentage;
    private EditText etWantsPercentage;
    private EditText etSavingsPercentage;
    private MaterialAutoCompleteTextView spinnerCurrency;
    private TextView tvTotalPercentage;
    private MaterialButton btnSaveSettings;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupTextWatchers();
        setupClickListeners();
        observeData();
    }

    private void initializeViews(View view) {
        etNecessitiesPercentage = view.findViewById(R.id.et_necessities_percentage);
        etWantsPercentage = view.findViewById(R.id.et_wants_percentage);
        etSavingsPercentage = view.findViewById(R.id.et_savings_percentage);
        spinnerCurrency = view.findViewById(R.id.spinner_currency);
        tvTotalPercentage = view.findViewById(R.id.tv_total_percentage);
        btnSaveSettings = view.findViewById(R.id.btn_save_settings);
        
        setupCurrencySpinner();
    }

    private void setupCurrencySpinner() {
        String[] currencies = {"RUB (Russian Ruble)", "USD (US Dollar)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_dropdown_item_1line, currencies);
        spinnerCurrency.setAdapter(adapter);
    }

    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateTotalPercentage();
            }
        };

        etNecessitiesPercentage.addTextChangedListener(textWatcher);
        etWantsPercentage.addTextChangedListener(textWatcher);
        etSavingsPercentage.addTextChangedListener(textWatcher);
    }

    private void setupClickListeners() {
        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void observeData() {
        settingsViewModel.getBudgetSettings().observe(getViewLifecycleOwner(), this::populateFields);
    }

    private void populateFields(BudgetSettings settings) {
        if (settings != null) {
            etNecessitiesPercentage.setText(String.valueOf(settings.getNecessitiesPercentage()));
            etWantsPercentage.setText(String.valueOf(settings.getWantsPercentage()));
            etSavingsPercentage.setText(String.valueOf(settings.getSavingsPercentage()));
            
            // Set currency selection
            String currency = settings.getCurrency();
            if ("RUB".equals(currency)) {
                spinnerCurrency.setText("RUB (Russian Ruble)", false);
            } else if ("USD".equals(currency)) {
                spinnerCurrency.setText("USD (US Dollar)", false);
            } else {
                spinnerCurrency.setText("RUB (Russian Ruble)", false); // Default to RUB
            }
            
            updateTotalPercentage();
        }
    }

    private void updateTotalPercentage() {
        try {
            double necessities = getPercentageFromEditText(etNecessitiesPercentage);
            double wants = getPercentageFromEditText(etWantsPercentage);
            double savings = getPercentageFromEditText(etSavingsPercentage);

            double total = necessities + wants + savings;
            tvTotalPercentage.setText(String.format("%.1f%%", total));

            // Change color based on whether total equals 100
            if (Math.abs(total - 100.0) < 0.1) { // Allow for small floating point errors
                tvTotalPercentage.setTextColor(getResources().getColor(R.color.md_theme_light_primary, null));
            } else {
                tvTotalPercentage.setTextColor(getResources().getColor(R.color.md_theme_light_error, null));
            }
        } catch (NumberFormatException e) {
            tvTotalPercentage.setText("0.0%");
            tvTotalPercentage.setTextColor(getResources().getColor(R.color.md_theme_light_error, null));
        }
    }

    private double getPercentageFromEditText(EditText editText) {
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return 0.0;
        }
        return Double.parseDouble(text);
    }

    private void saveSettings() {
        try {
            double necessities = getPercentageFromEditText(etNecessitiesPercentage);
            double wants = getPercentageFromEditText(etWantsPercentage);
            double savings = getPercentageFromEditText(etSavingsPercentage);

            double total = necessities + wants + savings;

            // Validate that total equals 100
            if (Math.abs(total - 100.0) >= 0.1) {
                Snackbar.make(requireView(), getString(R.string.percentage_total_error), 
                    Snackbar.LENGTH_LONG)
                    .show();
                return;
            }

            // Validate individual percentages
            if (necessities < 0 || wants < 0 || savings < 0) {
                Snackbar.make(requireView(), "Percentages cannot be negative", 
                    Snackbar.LENGTH_SHORT)
                    .show();
                return;
            }

            // Save currency
            String selectedCurrency = spinnerCurrency.getText().toString();
            String currencyCode = "RUB"; // Default
            if (selectedCurrency.startsWith("USD")) {
                currencyCode = "USD";
            } else if (selectedCurrency.startsWith("RUB")) {
                currencyCode = "RUB";
            }
            
            settingsViewModel.updatePercentages(necessities, wants, savings);
            settingsViewModel.updateCurrency(currencyCode);
            Snackbar.make(requireView(), getString(R.string.settings_saved), 
                Snackbar.LENGTH_SHORT)
                .show();

        } catch (NumberFormatException e) {
            Snackbar.make(requireView(), "Please enter valid numbers", 
                Snackbar.LENGTH_SHORT)
                .show();
        }
    }
}