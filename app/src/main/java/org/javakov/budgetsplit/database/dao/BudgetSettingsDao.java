package org.javakov.budgetsplit.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.OnConflictStrategy;

import org.javakov.budgetsplit.database.entities.BudgetSettings;

@Dao
public interface BudgetSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BudgetSettings budgetSettings);

    @Update
    void update(BudgetSettings budgetSettings);

    @Delete
    void delete(BudgetSettings budgetSettings);

    @Query("SELECT * FROM budget_settings WHERE id = 1")
    LiveData<BudgetSettings> getBudgetSettings();

    @Query("SELECT * FROM budget_settings WHERE id = 1")
    BudgetSettings getBudgetSettingsSync();

    @Query("UPDATE budget_settings SET total_balance = :totalBalance WHERE id = 1")
    void updateTotalBalance(double totalBalance);

    @Query("UPDATE budget_settings SET necessities_percentage = :necessities, wants_percentage = :wants, savings_percentage = :savings WHERE id = 1")
    void updatePercentages(double necessities, double wants, double savings);

    @Query("UPDATE budget_settings SET currency = :currency WHERE id = 1")
    void updateCurrency(String currency);
}