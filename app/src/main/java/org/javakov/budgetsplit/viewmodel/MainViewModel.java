package org.javakov.budgetsplit.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import org.javakov.budgetsplit.api.CurrencyExchangeService;
import org.javakov.budgetsplit.database.entities.BudgetSettings;
import org.javakov.budgetsplit.database.entities.Expense;
import org.javakov.budgetsplit.database.entities.Income;
import org.javakov.budgetsplit.database.entities.MoneySource;
import org.javakov.budgetsplit.repository.BudgetRepository;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private final BudgetRepository mRepository;
    private final LiveData<List<Income>> mAllIncomes;
    private final LiveData<List<Expense>> mAllExpenses;
    private final LiveData<BudgetSettings> mBudgetSettings;
    private final MediatorLiveData<Double> mTotalNecessities;
    private final MediatorLiveData<Double> mTotalWants;
    private final MediatorLiveData<Double> mTotalSavings;
    private final MediatorLiveData<Double> mTotalBalance;

    public MainViewModel(Application application) {
        super(application);
        mRepository = new BudgetRepository(application);
        
        // Initialize default settings if needed
        mRepository.initializeDefaultSettings();
        
        mAllIncomes = mRepository.getAllIncomes();
        mAllExpenses = mRepository.getAllExpenses();
        mBudgetSettings = mRepository.getBudgetSettings();
        
        // Get total balance from money sources with automatic currency conversion
        mTotalBalance = new MediatorLiveData<>();
        LiveData<List<MoneySource>> allMoneySources = mRepository.getAllMoneySources();
        mTotalBalance.addSource(allMoneySources, this::calculateTotalBalanceWithConversion);
        
        // Calculate distributions from total balance and percentages
        mTotalNecessities = new MediatorLiveData<>();
        mTotalNecessities.addSource(mBudgetSettings, settings -> calculateDistributions());
        mTotalNecessities.addSource(mTotalBalance, balance -> calculateDistributions());
        
        mTotalWants = new MediatorLiveData<>();
        mTotalWants.addSource(mBudgetSettings, settings -> calculateDistributions());
        mTotalWants.addSource(mTotalBalance, balance -> calculateDistributions());
        
        mTotalSavings = new MediatorLiveData<>();
        mTotalSavings.addSource(mBudgetSettings, settings -> calculateDistributions());
        mTotalSavings.addSource(mTotalBalance, balance -> calculateDistributions());
    }

    private void calculateTotalBalanceWithConversion(List<MoneySource> sources) {
        if (sources == null || sources.isEmpty()) {
            mTotalBalance.setValue(0.0);
            return;
        }
        
        double rubTotal = 0.0;
        double usdTotal = 0.0;
        
        // Sum up sources by currency
        for (MoneySource source : sources) {
            if ("RUB".equals(source.getCurrency()) || source.getCurrency() == null) {
                rubTotal += source.getAmount();
            } else if ("USD".equals(source.getCurrency())) {
                usdTotal += source.getAmount();
            }
        }
        
        if (usdTotal == 0) {
            // No USD sources, just set the RUB total immediately
            mTotalBalance.setValue(rubTotal);
        } else {
            // Set RUB total first as fallback, then try to convert USD
            mTotalBalance.setValue(rubTotal + usdTotal * 90.0); // Approximate fallback rate
            
            // Try to get real exchange rate for more accurate conversion
            final double finalRubTotal = rubTotal;
            final double finalUsdTotal = usdTotal;
            CurrencyExchangeService.getExchangeRate("USD", "RUB")
                .thenAccept(exchangeRate -> {
                    double convertedUsd = CurrencyExchangeService.convertAmount(finalUsdTotal, exchangeRate.rate);
                    mTotalBalance.setValue(finalRubTotal + convertedUsd);
                })
                .exceptionally(throwable -> {
                    // Keep the fallback value we already set
                    return null;
                });
        }
    }

    private void calculateDistributions() {
        BudgetSettings settings = mBudgetSettings.getValue();
        Double totalBalance = mTotalBalance.getValue();
        
        if (settings != null && totalBalance != null) {
            double necessitiesAmount = totalBalance * (settings.getNecessitiesPercentage() / 100.0);
            double wantsAmount = totalBalance * (settings.getWantsPercentage() / 100.0);
            double savingsAmount = totalBalance * (settings.getSavingsPercentage() / 100.0);
            
            mTotalNecessities.setValue(necessitiesAmount);
            mTotalWants.setValue(wantsAmount);
            mTotalSavings.setValue(savingsAmount);
        }
    }

    public LiveData<List<Income>> getAllIncomes() {
        return mAllIncomes;
    }

    public LiveData<List<Expense>> getAllExpenses() {
        return mAllExpenses;
    }

    public LiveData<BudgetSettings> getBudgetSettings() {
        return mBudgetSettings;
    }

    public LiveData<Double> getTotalNecessities() {
        return mTotalNecessities;
    }

    public LiveData<Double> getTotalWants() {
        return mTotalWants;
    }

    public LiveData<Double> getTotalSavings() {
        return mTotalSavings;
    }

    public LiveData<Double> getTotalBalance() {
        return mTotalBalance;
    }

    public void addIncomeWithSource(double amount, String description, String sourceName) {
        mRepository.addIncomeWithSourceAsync(amount, description, sourceName);
    }

    public void addExpenseWithSource(double amount, String description, String sourceName) {
        mRepository.addExpenseWithSourceAsync(amount, description, sourceName);
    }

    public void addMoneySource(String name, double amount, boolean isSavings, String currency) {
        long currentTime = System.currentTimeMillis();
        MoneySource moneySource = new MoneySource(name, amount, currentTime, isSavings, currency);
        mRepository.insertMoneySource(moneySource);
    }

    public void deleteMoneySource(MoneySource moneySource) {
        mRepository.deleteMoneySource(moneySource);
    }

    public void updateMoneySource(MoneySource moneySource) {
        mRepository.updateMoneySource(moneySource);
    }

    public LiveData<List<MoneySource>> getAllMoneySources() {
        return mRepository.getAllMoneySources();
    }
}