package org.javakov.budgetsplit.database.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "income_table")
public class Income {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "amount")
    public double amount;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "date")
    public long date;

    @ColumnInfo(name = "necessities_amount")
    public double necessitiesAmount;

    @ColumnInfo(name = "wants_amount")
    public double wantsAmount;

    @ColumnInfo(name = "savings_amount")
    public double savingsAmount;

    @ColumnInfo(name = "source_name")
    public String sourceName;

    @Ignore
    public Income(double amount, String description, long date,
                  double necessitiesAmount, double wantsAmount, double savingsAmount) {
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.necessitiesAmount = necessitiesAmount;
        this.wantsAmount = wantsAmount;
        this.savingsAmount = savingsAmount;
        this.sourceName = null;
    }

    public Income(double amount, String description, long date, 
                  double necessitiesAmount, double wantsAmount, double savingsAmount, String sourceName) {
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.necessitiesAmount = necessitiesAmount;
        this.wantsAmount = wantsAmount;
        this.savingsAmount = savingsAmount;
        this.sourceName = sourceName;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public double getNecessitiesAmount() { return necessitiesAmount; }
    public void setNecessitiesAmount(double necessitiesAmount) { this.necessitiesAmount = necessitiesAmount; }

    public double getWantsAmount() { return wantsAmount; }
    public void setWantsAmount(double wantsAmount) { this.wantsAmount = wantsAmount; }

    public double getSavingsAmount() { return savingsAmount; }
    public void setSavingsAmount(double savingsAmount) { this.savingsAmount = savingsAmount; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
}