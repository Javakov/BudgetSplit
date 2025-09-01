package org.javakov.budgetsplit.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "distribution_table")
public class Distribution {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "amount")
    public double amount;

    @ColumnInfo(name = "percentage")
    public double percentage;

    @ColumnInfo(name = "date")
    public long date;

    @ColumnInfo(name = "income_id")
    public int incomeId;

    public Distribution(String category, double amount, double percentage, long date, int incomeId) {
        this.category = category;
        this.amount = amount;
        this.percentage = percentage;
        this.date = date;
        this.incomeId = incomeId;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public int getIncomeId() { return incomeId; }
    public void setIncomeId(int incomeId) { this.incomeId = incomeId; }
}