package org.javakov.budgetsplit.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

@Entity(tableName = "money_source_table")
public class MoneySource {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "amount")
    public double amount;

    @ColumnInfo(name = "date_created")
    public long dateCreated;

    @ColumnInfo(name = "is_savings", defaultValue = "0")
    public boolean isSavings;

    @ColumnInfo(name = "currency", defaultValue = "'RUB'")
    public String currency;

    @Ignore
    public MoneySource(String name, double amount, long dateCreated) {
        this.name = name;
        this.amount = amount;
        this.dateCreated = dateCreated;
        this.isSavings = false;
        this.currency = "RUB";
    }

    @Ignore
    public MoneySource(String name, double amount, long dateCreated, boolean isSavings) {
        this.name = name;
        this.amount = amount;
        this.dateCreated = dateCreated;
        this.isSavings = isSavings;
        this.currency = "RUB";
    }

    public MoneySource(String name, double amount, long dateCreated, boolean isSavings, String currency) {
        this.name = name;
        this.amount = amount;
        this.dateCreated = dateCreated;
        this.isSavings = isSavings;
        this.currency = currency != null ? currency : "RUB";
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public boolean isSavings() { return isSavings; }
    public void setSavings(boolean savings) { this.isSavings = savings; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}