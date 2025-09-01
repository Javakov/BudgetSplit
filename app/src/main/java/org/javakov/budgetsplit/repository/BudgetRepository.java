package org.javakov.budgetsplit.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import org.javakov.budgetsplit.database.BudgetDatabase;
import org.javakov.budgetsplit.database.dao.BudgetSettingsDao;
import org.javakov.budgetsplit.database.dao.DistributionDao;
import org.javakov.budgetsplit.database.dao.ExpenseDao;
import org.javakov.budgetsplit.database.dao.IncomeDao;
import org.javakov.budgetsplit.database.dao.MoneySourceDao;
import org.javakov.budgetsplit.database.entities.BudgetSettings;
import org.javakov.budgetsplit.database.entities.Distribution;
import org.javakov.budgetsplit.database.entities.Expense;
import org.javakov.budgetsplit.database.entities.Income;
import org.javakov.budgetsplit.database.entities.MoneySource;

import java.util.List;

public class BudgetRepository {
    private final IncomeDao mIncomeDao;
    private final DistributionDao mDistributionDao;
    private final BudgetSettingsDao mBudgetSettingsDao;
    private final MoneySourceDao mMoneySourceDao;
    private final ExpenseDao mExpenseDao;
    private final LiveData<List<Income>> mAllIncomes;
    private final LiveData<List<Expense>> mAllExpenses;
    private final LiveData<BudgetSettings> mBudgetSettings;
    private final LiveData<List<MoneySource>> mAllMoneySources;

    public BudgetRepository(Application application) {
        BudgetDatabase db = BudgetDatabase.getDatabase(application);
        mIncomeDao = db.incomeDao();
        mDistributionDao = db.distributionDao();
        mBudgetSettingsDao = db.budgetSettingsDao();
        mMoneySourceDao = db.moneySourceDao();
        mExpenseDao = db.expenseDao();
        mAllIncomes = mIncomeDao.getAllIncomes();
        mAllExpenses = mExpenseDao.getAllExpenses();
        mBudgetSettings = mBudgetSettingsDao.getBudgetSettings();
        mAllMoneySources = mMoneySourceDao.getAllMoneySources();
    }

    // Income operations
    public LiveData<List<Income>> getAllIncomes() {
        return mAllIncomes;
    }

    // Expense operations
    public LiveData<List<Expense>> getAllExpenses() {
        return mAllExpenses;
    }

    public void insert(Income income) {
        BudgetDatabase.databaseWriteExecutor.execute(() -> mIncomeDao.insert(income));
    }

    public void insertIncomeWithDistributions(Income income, BudgetSettings settings) {
        BudgetDatabase.databaseWriteExecutor.execute(() -> {
            long incomeId = mIncomeDao.insert(income);
            
            // Create distribution records
            Distribution necessities = new Distribution("Necessities", income.getNecessitiesAmount(), 
                settings.getNecessitiesPercentage(), income.getDate(), (int) incomeId);
            Distribution wants = new Distribution("Wants", income.getWantsAmount(), 
                settings.getWantsPercentage(), income.getDate(), (int) incomeId);
            Distribution savings = new Distribution("Savings", income.getSavingsAmount(), 
                settings.getSavingsPercentage(), income.getDate(), (int) incomeId);
            
            mDistributionDao.insert(necessities);
            mDistributionDao.insert(wants);
            mDistributionDao.insert(savings);
        });
    }

    public void addIncomeWithSourceAsync(double amount, String description, String sourceName) {
        BudgetDatabase.databaseWriteExecutor.execute(() -> {
            BudgetSettings settings = mBudgetSettingsDao.getBudgetSettingsSync();
            if (settings == null) {
                settings = new BudgetSettings(1, 50.0, 30.0, 20.0, 0.0, "RUB");
                mBudgetSettingsDao.insert(settings);
            }
            
            long currentTime = System.currentTimeMillis();
            
            double necessitiesAmount = amount * (settings.getNecessitiesPercentage() / 100.0);
            double wantsAmount = amount * (settings.getWantsPercentage() / 100.0);
            double savingsAmount = amount * (settings.getSavingsPercentage() / 100.0);
            
            Income income = new Income(amount, description, currentTime, 
                necessitiesAmount, wantsAmount, savingsAmount, sourceName);
            
            long incomeId = mIncomeDao.insert(income);
            
            // Create distribution records
            Distribution necessities = new Distribution("Necessities", necessitiesAmount, 
                settings.getNecessitiesPercentage(), currentTime, (int) incomeId);
            Distribution wants = new Distribution("Wants", wantsAmount, 
                settings.getWantsPercentage(), currentTime, (int) incomeId);
            Distribution savings = new Distribution("Savings", savingsAmount, 
                settings.getSavingsPercentage(), currentTime, (int) incomeId);
            
            mDistributionDao.insert(necessities);
            mDistributionDao.insert(wants);
            mDistributionDao.insert(savings);
            
            // Update the money source balance
            List<MoneySource> sources = mMoneySourceDao.getAllMoneySourcesSync();
            if (sources != null) {
                for (MoneySource source : sources) {
                    if (source.getName().equals(sourceName)) {
                        source.setAmount(source.getAmount() + amount);
                        mMoneySourceDao.update(source);
                        break;
                    }
                }
            }
        });
    }

    public void addExpenseWithSourceAsync(double amount, String description, String sourceName) {
        BudgetDatabase.databaseWriteExecutor.execute(() -> {
            long currentTime = System.currentTimeMillis();
            
            // Create expense record
            Expense expense = new Expense(amount, description, currentTime, sourceName);
            mExpenseDao.insert(expense);
            
            // Update the money source balance (subtract expense)
            List<MoneySource> sources = mMoneySourceDao.getAllMoneySourcesSync();
            if (sources != null) {
                for (MoneySource source : sources) {
                    if (source.getName().equals(sourceName)) {
                        double newAmount = source.getAmount() - amount;
                        if (newAmount >= 0) {  // Only allow if sufficient balance
                            source.setAmount(newAmount);
                            mMoneySourceDao.update(source);
                        }
                        break;
                    }
                }
            }
        });
    }

    public LiveData<Double> getTotalNecessitiesAmount() {
        return mIncomeDao.getTotalNecessitiesAmount();
    }

    public LiveData<Double> getTotalWantsAmount() {
        return mIncomeDao.getTotalWantsAmount();
    }

    public LiveData<Double> getTotalSavingsAmount() {
        return mIncomeDao.getTotalSavingsAmount();
    }

    // Budget Settings operations
    public LiveData<BudgetSettings> getBudgetSettings() {
        return mBudgetSettings;
    }

    public void updateTotalBalance(double totalBalance) {
        BudgetDatabase.databaseWriteExecutor.execute(() ->
                mBudgetSettingsDao.updateTotalBalance(totalBalance));
    }

    public void updatePercentages(double necessities, double wants, double savings) {
        BudgetDatabase.databaseWriteExecutor.execute(() ->
                mBudgetSettingsDao.updatePercentages(necessities, wants, savings));
    }

    public void updateCurrency(String currency) {
        BudgetDatabase.databaseWriteExecutor.execute(() ->
                mBudgetSettingsDao.updateCurrency(currency));
    }

    public BudgetSettings getBudgetSettingsSync() {
        BudgetSettings settings = mBudgetSettingsDao.getBudgetSettingsSync();
        if (settings == null) {
            // Create default settings synchronously if none exist
            BudgetSettings defaultSettings = new BudgetSettings(1, 50.0, 30.0, 20.0, 0.0, "RUB");
            mBudgetSettingsDao.insert(defaultSettings);
            return defaultSettings;
        }
        return settings;
    }

    public void initializeDefaultSettings() {
        BudgetDatabase.databaseWriteExecutor.execute(() -> {
            BudgetSettings settings = mBudgetSettingsDao.getBudgetSettingsSync();
            if (settings == null) {
                BudgetSettings defaultSettings = new BudgetSettings(1, 50.0, 30.0, 20.0, 0.0, "RUB");
                mBudgetSettingsDao.insert(defaultSettings);
            }
        });
    }

    public void addIncomeAsync(double amount, String description) {
        BudgetDatabase.databaseWriteExecutor.execute(() -> {
            BudgetSettings settings = mBudgetSettingsDao.getBudgetSettingsSync();
            if (settings == null) {
                // Create default settings if none exist
                settings = new BudgetSettings(1, 50.0, 30.0, 20.0, 0.0, "RUB");
                mBudgetSettingsDao.insert(settings);
            }
            
            long currentTime = System.currentTimeMillis();
            
            double necessitiesAmount = amount * (settings.getNecessitiesPercentage() / 100.0);
            double wantsAmount = amount * (settings.getWantsPercentage() / 100.0);
            double savingsAmount = amount * (settings.getSavingsPercentage() / 100.0);
            
            Income income = new Income(amount, description, currentTime, 
                necessitiesAmount, wantsAmount, savingsAmount);
            
            long incomeId = mIncomeDao.insert(income);
            
            // Create distribution records
            Distribution necessities = new Distribution("Necessities", necessitiesAmount, 
                settings.getNecessitiesPercentage(), currentTime, (int) incomeId);
            Distribution wants = new Distribution("Wants", wantsAmount, 
                settings.getWantsPercentage(), currentTime, (int) incomeId);
            Distribution savings = new Distribution("Savings", savingsAmount, 
                settings.getSavingsPercentage(), currentTime, (int) incomeId);
            
            mDistributionDao.insert(necessities);
            mDistributionDao.insert(wants);
            mDistributionDao.insert(savings);
        });
    }

    // Distribution operations
    public LiveData<List<Distribution>> getAllDistributions() {
        return mDistributionDao.getAllDistributions();
    }

    public LiveData<Double> getTotalAmountByCategory(String category) {
        return mDistributionDao.getTotalAmountByCategory(category);
    }

    // MoneySource operations
    public LiveData<List<MoneySource>> getAllMoneySources() {
        return mAllMoneySources;
    }

    public LiveData<Double> getTotalMoneySourceAmount() {
        return mMoneySourceDao.getTotalAmount();
    }

    public void insertMoneySource(MoneySource moneySource) {
        BudgetDatabase.databaseWriteExecutor.execute(() ->
                mMoneySourceDao.insert(moneySource));
    }

    public void updateMoneySource(MoneySource moneySource) {
        BudgetDatabase.databaseWriteExecutor.execute(() ->
                mMoneySourceDao.update(moneySource));
    }

    public void deleteMoneySource(MoneySource moneySource) {
        BudgetDatabase.databaseWriteExecutor.execute(() ->
                mMoneySourceDao.delete(moneySource));
    }
}