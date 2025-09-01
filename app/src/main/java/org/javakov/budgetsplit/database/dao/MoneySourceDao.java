package org.javakov.budgetsplit.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.javakov.budgetsplit.database.entities.MoneySource;

import java.util.List;

@Dao
public interface MoneySourceDao {
    @Insert
    long insert(MoneySource moneySource);

    @Update
    void update(MoneySource moneySource);

    @Delete
    void delete(MoneySource moneySource);

    @Query("DELETE FROM money_source_table")
    void deleteAllMoneySources();

    @Query("SELECT * FROM money_source_table ORDER BY date_created DESC")
    LiveData<List<MoneySource>> getAllMoneySources();

    @Query("SELECT SUM(amount) FROM money_source_table")
    LiveData<Double> getTotalAmount();

    @Query("SELECT * FROM money_source_table WHERE id = :id")
    LiveData<MoneySource> getMoneySourceById(int id);

    @Query("SELECT * FROM money_source_table ORDER BY date_created DESC")
    List<MoneySource> getAllMoneySourcesSync();
}