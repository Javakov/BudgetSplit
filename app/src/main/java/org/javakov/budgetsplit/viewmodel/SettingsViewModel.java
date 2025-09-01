package org.javakov.budgetsplit.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.javakov.budgetsplit.database.entities.BudgetSettings;
import org.javakov.budgetsplit.repository.BudgetRepository;

public class SettingsViewModel extends AndroidViewModel {
    private final BudgetRepository mRepository;
    private final LiveData<BudgetSettings> mBudgetSettings;

    public SettingsViewModel(Application application) {
        super(application);
        mRepository = new BudgetRepository(application);
        
        // Initialize default settings if needed
        mRepository.initializeDefaultSettings();
        
        mBudgetSettings = mRepository.getBudgetSettings();
    }

    public LiveData<BudgetSettings> getBudgetSettings() {
        return mBudgetSettings;
    }

    public void updatePercentages(double necessities, double wants, double savings) {
        mRepository.updatePercentages(necessities, wants, savings);
    }

    public void updateCurrency(String currency) {
        mRepository.updateCurrency(currency);
    }
}