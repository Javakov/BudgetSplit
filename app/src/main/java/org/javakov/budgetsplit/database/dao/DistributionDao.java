package org.javakov.budgetsplit.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.javakov.budgetsplit.database.entities.Distribution;

import java.util.List;

@Dao
public interface DistributionDao {
    @Insert
    void insert(Distribution distribution);

    @Update
    void update(Distribution distribution);

    @Delete
    void delete(Distribution distribution);

    @Query("DELETE FROM distribution_table")
    void deleteAllDistributions();

    @Query("SELECT * FROM distribution_table ORDER BY date DESC")
    LiveData<List<Distribution>> getAllDistributions();

    @Query("SELECT * FROM distribution_table WHERE income_id = :incomeId")
    LiveData<List<Distribution>> getDistributionsByIncomeId(int incomeId);

    @Query("SELECT SUM(amount) FROM distribution_table WHERE category = :category")
    LiveData<Double> getTotalAmountByCategory(String category);
}