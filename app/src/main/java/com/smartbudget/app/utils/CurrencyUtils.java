package com.smartbudget.app.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {

    private static final NumberFormat VND_FORMAT = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final DecimalFormat SIMPLE_FORMAT = new DecimalFormat("#,###");

    public static String formatVND(double amount) {
        return SIMPLE_FORMAT.format(amount) + " ₫";
    }

    public static String formatVNDWithSign(double amount, boolean isExpense) {
        String sign = isExpense ? "-" : "+";
        return sign + SIMPLE_FORMAT.format(Math.abs(amount)) + " ₫";
    }

    public static String formatVNDCompact(double amount) {
        if (amount >= 1_000_000_000) {
            return SIMPLE_FORMAT.format(amount / 1_000_000_000) + " tỷ";
        } else if (amount >= 1_000_000) {
            return SIMPLE_FORMAT.format(amount / 1_000_000) + " tr";
        } else if (amount >= 1_000) {
            return SIMPLE_FORMAT.format(amount / 1_000) + "k";
        }
        return SIMPLE_FORMAT.format(amount) + " ₫";
    }

    public static double parseAmount(String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }

        // Remove all non-numeric characters except decimal point
        String cleaned = input.replaceAll("[^\\d.]", "");

        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String formatNumber(double value) {
        return SIMPLE_FORMAT.format(value);
    }
}
