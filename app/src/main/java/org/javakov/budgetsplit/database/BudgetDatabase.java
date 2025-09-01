package org.javakov.budgetsplit.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.javakov.budgetsplit.database.dao.BudgetSettingsDao;
import org.javakov.budgetsplit.database.dao.DistributionDao;
import org.javakov.budgetsplit.database.dao.ExpenseDao;
import org.javakov.budgetsplit.database.dao.IncomeDao;
import org.javakov.budgetsplit.database.dao.MoneySourceDao;
import org.javakov.budgetsplit.database.entities.BudgetSettings;
import org.javakov.budgetsplit.database.entities.Distribution;
import org.javakov.budgetsplit.database.entities.Expense;
import org.javakov.budgetsplit.database.entities.Income;
import org.javakov.budgetsplit.database.entities.MoneySource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {Income.class, Distribution.class, BudgetSettings.class, MoneySource.class, Expense.class},
    version = 5,
    exportSchema = false
)
public abstract class BudgetDatabase extends RoomDatabase {
    
    public abstract IncomeDao incomeDao();
    public abstract DistributionDao distributionDao();
    public abstract BudgetSettingsDao budgetSettingsDao();
    public abstract MoneySourceDao moneySourceDao();
    public abstract ExpenseDao expenseDao();

    private static volatile BudgetDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = 
        Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static BudgetDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (BudgetDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            BudgetDatabase.class, "budget_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // Initialize default budget settings
            databaseWriteExecutor.execute(() -> {
                // Populate the database with default settings
                BudgetSettingsDao dao = INSTANCE.budgetSettingsDao();
                BudgetSettings defaultSettings = new BudgetSettings(1, 50.0, 30.0, 20.0, 0.0, "RUB");
                dao.insert(defaultSettings);
            });
        }
    };
}