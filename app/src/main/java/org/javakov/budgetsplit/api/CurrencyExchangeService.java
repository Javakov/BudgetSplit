package org.javakov.budgetsplit.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.json.JSONObject;

public class CurrencyExchangeService {
    private static final String API_KEY = "5109902dfe700372f3b325f3";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/";
    private static final Executor executor = Executors.newSingleThreadExecutor();
    
    public static class ExchangeRate {
        public final double rate;
        public final String fromCurrency;
        public final String toCurrency;
        
        public ExchangeRate(double rate, String fromCurrency, String toCurrency) {
            this.rate = rate;
            this.fromCurrency = fromCurrency;
            this.toCurrency = toCurrency;
        }
    }
    
    public static CompletableFuture<ExchangeRate> getExchangeRate(String fromCurrency, String toCurrency) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String requestUrl = BASE_URL + fromCurrency;
                android.util.Log.d("CurrencyExchange", "Making request to: " + requestUrl);
                
                URL url = new URL(requestUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    
                    // Check if the API call was successful
                    String result = jsonResponse.getString("result");
                    if (!"success".equals(result)) {
                        String errorType = jsonResponse.optString("error-type", "unknown");
                        throw new RuntimeException("API error: " + errorType);
                    }
                    
                    JSONObject rates = jsonResponse.getJSONObject("conversion_rates");
                    
                    if (rates.has(toCurrency)) {
                        double rate = rates.getDouble(toCurrency);
                        return new ExchangeRate(rate, fromCurrency, toCurrency);
                    } else {
                        throw new RuntimeException("Currency not supported: " + toCurrency);
                    }
                } else {
                    throw new RuntimeException("API request failed with code: " + responseCode);
                }
            } catch (IOException e) {
                android.util.Log.e("CurrencyExchange", "Network error: " + e.getMessage(), e);
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (org.json.JSONException e) {
                android.util.Log.e("CurrencyExchange", "JSON parsing error: " + e.getMessage(), e);
                throw new RuntimeException("Failed to parse API response: " + e.getMessage(), e);
            } catch (Exception e) {
                android.util.Log.e("CurrencyExchange", "Unexpected error: " + e.getMessage(), e);
                throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
            }
        }, executor);
    }
    
    public static double convertAmount(double amount, double exchangeRate) {
        return amount * exchangeRate;
    }
}