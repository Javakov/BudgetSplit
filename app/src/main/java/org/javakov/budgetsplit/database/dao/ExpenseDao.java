package org.javakov.budgetsplit.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.javakov.budgetsplit.database.entities.Expense;

import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    long insert(Expense expense);

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);

    @Query("DELETE FROM expense_table")
    void deleteAllExpenses();

    @Query("SELECT * FROM expense_table ORDER BY date DESC")
    LiveData<List<Expense>> getAllExpenses();

    @Query("SELECT SUM(amount) FROM expense_table")
    LiveData<Double> getTotalExpenses();

    @Query("SELECT * FROM expense_table WHERE id = :id")
    LiveData<Expense> getExpenseById(int id);
}