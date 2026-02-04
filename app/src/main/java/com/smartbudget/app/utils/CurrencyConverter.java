package com.smartbudget.app.utils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Currency converter helper.
 * Converts between different currencies.
 */
public class CurrencyConverter {

    // Exchange rates (relative to VND)
    private static final Map<String, Double> EXCHANGE_RATES = new HashMap<>();
    
    static {
        EXCHANGE_RATES.put("VND", 1.0);
        EXCHANGE_RATES.put("USD", 24500.0);
        EXCHANGE_RATES.put("EUR", 26500.0);
        EXCHANGE_RATES.put("GBP", 31000.0);
        EXCHANGE_RATES.put("JPY", 165.0);
        EXCHANGE_RATES.put("KRW", 18.5);
        EXCHANGE_RATES.put("CNY", 3400.0);
        EXCHANGE_RATES.put("THB", 700.0);
        EXCHANGE_RATES.put("SGD", 18200.0);
        EXCHANGE_RATES.put("AUD", 16000.0);
    }

    public static class Currency {
        public final String code;
        public final String symbol;
        public final String name;
        public final String flag;

        public Currency(String code, String symbol, String name, String flag) {
            this.code = code;
            this.symbol = symbol;
            this.name = name;
            this.flag = flag;
        }
    }

    public static final Currency[] CURRENCIES = {
        new Currency("VND", "₫", "Việt Nam Đồng", "🇻🇳"),
        new Currency("USD", "$", "US Dollar", "🇺🇸"),
        new Currency("EUR", "€", "Euro", "🇪🇺"),
        new Currency("GBP", "£", "British Pound", "🇬🇧"),
        new Currency("JPY", "¥", "Japanese Yen", "🇯🇵"),
        new Currency("KRW", "₩", "Korean Won", "🇰🇷"),
        new Currency("CNY", "¥", "Chinese Yuan", "🇨🇳"),
        new Currency("THB", "฿", "Thai Baht", "🇹🇭"),
        new Currency("SGD", "S$", "Singapore Dollar", "🇸🇬"),
        new Currency("AUD", "A$", "Australian Dollar", "🇦🇺")
    };

    /**
     * Convert amount from one currency to another.
     */
    public static double convert(double amount, String from, String to) {
        Double fromRate = EXCHANGE_RATES.get(from);
        Double toRate = EXCHANGE_RATES.get(to);

        if (fromRate == null || toRate == null) {
            return amount;
        }

        // Convert to VND first, then to target currency
        double vndAmount = amount * fromRate;
        return vndAmount / toRate;
    }

    /**
     * Format amount with currency symbol.
     */
    public static String format(double amount, String currencyCode) {
        Currency currency = getCurrency(currencyCode);
        DecimalFormat formatter = new DecimalFormat("#,###.##");
        
        if (currencyCode.equals("VND")) {
            return formatter.format(amount) + " " + currency.symbol;
        } else {
            return currency.symbol + formatter.format(amount);
        }
    }

    /**
     * Get currency by code.
     */
    public static Currency getCurrency(String code) {
        for (Currency currency : CURRENCIES) {
            if (currency.code.equals(code)) {
                return currency;
            }
        }
        return CURRENCIES[0]; // Default to VND
    }

    /**
     * Quick conversion from VND.
     */
    public static String convertFromVND(double vndAmount, String toCurrency) {
        double converted = convert(vndAmount, "VND", toCurrency);
        return format(converted, toCurrency);
    }

    /**
     * Quick conversion to VND.
     */
    public static double convertToVND(double amount, String fromCurrency) {
        return convert(amount, fromCurrency, "VND");
    }
}
