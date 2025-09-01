package org.javakov.budgetsplit.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import org.javakov.budgetsplit.database.entities.BudgetSettings;
import org.javakov.budgetsplit.database.entities.Expense;
import org.javakov.budgetsplit.database.entities.Income;
import org.javakov.budgetsplit.database.entities.MoneySource;
import org.javakov.budgetsplit.repository.BudgetRepository;

import java.util.List;
import java.util.Objects;

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
        
        // Get total balance from money sources
        mTotalBalance = new MediatorLiveData<>();
        LiveData<Double> totalMoneySourceAmount = mRepository.getTotalMoneySourceAmount();
        mTotalBalance.addSource(totalMoneySourceAmount, amount ->
                mTotalBalance.setValue(Objects.requireNonNullElse(amount, 0.0)));
        
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

    public void addIncome(double amount, String description) {
        mRepository.addIncomeAsync(amount, description);
    }

    public void addIncomeWithSource(double amount, String description, String sourceName) {
        mRepository.addIncomeWithSourceAsync(amount, description, sourceName);
    }

    public void addExpenseWithSource(double amount, String description, String sourceName) {
        mRepository.addExpenseWithSourceAsync(amount, description, sourceName);
    }

    public void addMoneySource(String name, double amount) {
        long currentTime = System.currentTimeMillis();
        MoneySource moneySource = new MoneySource(name, amount, currentTime);
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