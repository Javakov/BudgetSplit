package org.javakov.budgetsplit.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.javakov.budgetsplit.database.entities.Income;

import java.util.List;

@Dao
public interface IncomeDao {
    @Insert
    long insert(Income income);

    @Update
    void update(Income income);

    @Delete
    void delete(Income income);

    @Query("DELETE FROM income_table")
    void deleteAllIncomes();

    @Query("SELECT * FROM income_table ORDER BY date DESC")
    LiveData<List<Income>> getAllIncomes();

    @Query("SELECT SUM(amount) FROM income_table")
    LiveData<Double> getTotalIncomeAmount();

    @Query("SELECT SUM(necessities_amount) FROM income_table")
    LiveData<Double> getTotalNecessitiesAmount();

    @Query("SELECT SUM(wants_amount) FROM income_table")
    LiveData<Double> getTotalWantsAmount();

    @Query("SELECT SUM(savings_amount) FROM income_table")
    LiveData<Double> getTotalSavingsAmount();

    @Query("SELECT * FROM income_table WHERE id = :id")
    LiveData<Income> getIncomeById(int id);
}