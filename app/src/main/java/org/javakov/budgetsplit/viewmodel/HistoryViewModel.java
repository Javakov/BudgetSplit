package org.javakov.budgetsplit.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.javakov.budgetsplit.database.entities.BudgetSettings;
import org.javakov.budgetsplit.database.entities.Expense;
import org.javakov.budgetsplit.database.entities.Income;
import org.javakov.budgetsplit.repository.BudgetRepository;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private final LiveData<List<Income>> mAllIncomes;
    private final LiveData<List<Expense>> mAllExpenses;
    private final LiveData<BudgetSettings> mBudgetSettings;

    public HistoryViewModel(Application application) {
        super(application);
        BudgetRepository mRepository = new BudgetRepository(application);
        mAllIncomes = mRepository.getAllIncomes();
        mAllExpenses = mRepository.getAllExpenses();
        mBudgetSettings = mRepository.getBudgetSettings();
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
}