package org.javakov.budgetsplit;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.javakov.budgetsplit.ui.fragments.HistoryFragment;
import org.javakov.budgetsplit.ui.fragments.MainFragment;
import org.javakov.budgetsplit.ui.fragments.SettingsFragment;
import org.javakov.budgetsplit.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private BottomNavigationView bottomNavigation;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        initializeViews();
        setupToolbar();
        setupBottomNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new MainFragment());
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.navigation_home));
        }
    }

    private void updateToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = getString(R.string.app_name);
            int itemId = item.getItemId();
            
            if (itemId == R.id.navigation_home) {
                selectedFragment = new MainFragment();
                title = getString(R.string.navigation_home);
            } else if (itemId == R.id.navigation_history) {
                selectedFragment = new HistoryFragment();
                title = getString(R.string.navigation_history);
            } else if (itemId == R.id.navigation_settings) {
                selectedFragment = new SettingsFragment();
                title = getString(R.string.navigation_settings);
            }
            
            if (selectedFragment != null) {
                updateToolbarTitle(title);
                return loadFragment(selectedFragment);
            }
            return false;
        });
    }


    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    public void showAddIncomeDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_income, null);
        
        EditText etAmount = dialogView.findViewById(R.id.et_amount);
        EditText etDescription = dialogView.findViewById(R.id.et_description);
        MaterialAutoCompleteTextView spinnerMoneySource = dialogView.findViewById(R.id.spinner_money_source);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnAdd = dialogView.findViewById(R.id.btn_add);

        // Setup money source dropdown
        mainViewModel.getAllMoneySources().observe(this, moneySources -> {
            if (moneySources != null && !moneySources.isEmpty()) {
                String[] sourceNames = new String[moneySources.size()];
                for (int i = 0; i < moneySources.size(); i++) {
                    sourceNames[i] = moneySources.get(i).getName();
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                    android.R.layout.simple_dropdown_item_1line, sourceNames);
                spinnerMoneySource.setAdapter(adapter);
            }
        });

        Dialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnAdd.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String selectedSource = spinnerMoneySource.getText().toString().trim();
            
            if (TextUtils.isEmpty(amountStr)) {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.invalid_amount), Snackbar.LENGTH_SHORT)
                    .show();
                return;
            }
            
            if (TextUtils.isEmpty(selectedSource)) {
                Snackbar.make(findViewById(android.R.id.content), "Please select a money source", Snackbar.LENGTH_SHORT)
                    .show();
                return;
            }
            
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.invalid_amount), Snackbar.LENGTH_SHORT)
                        .show();
                    return;
                }
                
                if (TextUtils.isEmpty(description)) {
                    description = "Income";
                }
                
                mainViewModel.addIncomeWithSource(amount, description, selectedSource);
                dialog.dismiss();
                Snackbar.make(findViewById(android.R.id.content), "Income added successfully", Snackbar.LENGTH_SHORT)
                    .show();
                
            } catch (NumberFormatException e) {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.invalid_amount), Snackbar.LENGTH_SHORT)
                    .show();
            }
        });

        dialog.show();
    }
}