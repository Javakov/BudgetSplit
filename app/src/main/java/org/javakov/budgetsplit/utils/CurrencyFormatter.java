package org.javakov.budgetsplit.utils;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {
    
    private static final DecimalFormat RUB_FORMAT = new DecimalFormat("#,##0.00 ₽");
    private static final DecimalFormat USD_FORMAT = new DecimalFormat("$#,##0.00");
    
    public static String formatCurrency(double amount, String currencyCode) {
        if (currencyCode == null) {
            currencyCode = "RUB";
        }
        
        switch (currencyCode) {
            case "USD":
                return USD_FORMAT.format(amount);
            case "RUB":
            default:
                return RUB_FORMAT.format(amount);
        }
    }
    
    public static NumberFormat getCurrencyFormatter(String currencyCode) {
        if (currencyCode == null) {
            currencyCode = "RUB";
        }
        
        switch (currencyCode) {
            case "USD":
                return NumberFormat.getCurrencyInstance(Locale.US);
            case "RUB":
            default:
                // Create custom formatter for Rubles
                return new NumberFormat() {
                    @NonNull
                    @Override
                    public StringBuffer format(double number, @NonNull StringBuffer toAppendTo, @NonNull java.text.FieldPosition pos) {
                        return RUB_FORMAT.format(number, toAppendTo, pos);
                    }

                    @NonNull
                    @Override
                    public StringBuffer format(long number, @NonNull StringBuffer toAppendTo, @NonNull java.text.FieldPosition pos) {
                        return RUB_FORMAT.format(number, toAppendTo, pos);
                    }

                    @Override
                    public Number parse(@NonNull String source, @NonNull java.text.ParsePosition parsePosition) {
                        return RUB_FORMAT.parse(source, parsePosition);
                    }
                };
        }
    }
}