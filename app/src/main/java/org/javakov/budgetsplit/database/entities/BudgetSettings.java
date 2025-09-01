package org.javakov.budgetsplit.database.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "budget_settings")
public class BudgetSettings {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "necessities_percentage")
    public double necessitiesPercentage;

    @ColumnInfo(name = "wants_percentage")
    public double wantsPercentage;

    @ColumnInfo(name = "savings_percentage")
    public double savingsPercentage;

    @ColumnInfo(name = "total_balance")
    public double totalBalance;

    @ColumnInfo(name = "currency")
    public String currency;

    public BudgetSettings(int id, double necessitiesPercentage, double wantsPercentage, 
                         double savingsPercentage, double totalBalance) {
        this.id = id;
        this.necessitiesPercentage = necessitiesPercentage;
        this.wantsPercentage = wantsPercentage;
        this.savingsPercentage = savingsPercentage;
        this.totalBalance = totalBalance;
        this.currency = "RUB"; // Default to Ruble
    }

    @Ignore
    public BudgetSettings(int id, double necessitiesPercentage, double wantsPercentage, 
                         double savingsPercentage, double totalBalance, String currency) {
        this.id = id;
        this.necessitiesPercentage = necessitiesPercentage;
        this.wantsPercentage = wantsPercentage;
        this.savingsPercentage = savingsPercentage;
        this.totalBalance = totalBalance;
        this.currency = currency;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getNecessitiesPercentage() { return necessitiesPercentage; }
    public void setNecessitiesPercentage(double necessitiesPercentage) { 
        this.necessitiesPercentage = necessitiesPercentage; 
    }

    public double getWantsPercentage() { return wantsPercentage; }
    public void setWantsPercentage(double wantsPercentage) { 
        this.wantsPercentage = wantsPercentage; 
    }

    public double getSavingsPercentage() { return savingsPercentage; }
    public void setSavingsPercentage(double savingsPercentage) { 
        this.savingsPercentage = savingsPercentage; 
    }

    public double getTotalBalance() { return totalBalance; }
    public void setTotalBalance(double totalBalance) { 
        this.totalBalance = totalBalance; 
    }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { 
        this.currency = currency; 
    }
}