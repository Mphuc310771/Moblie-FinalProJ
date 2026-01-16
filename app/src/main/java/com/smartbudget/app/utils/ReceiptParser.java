package com.smartbudget.app.utils;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to parse receipt text extracted from OCR
 * Focuses on Vietnamese receipt formats
 */
public class ReceiptParser {

    // Common patterns for Vietnamese receipts
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(?:tổng|total|thanh toán|thành tiền|t\\.tiền|amount)[:\\s]*([\\d.,]+)",
            Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern AMOUNT_VND_PATTERN = Pattern.compile(
            "([\\d.,]+)\\s*(?:đ|₫|vnd|vnđ|dong)",
            Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern LARGE_AMOUNT_PATTERN = Pattern.compile(
            "([\\d]{1,3}(?:[.,]\\d{3})+)"
    );
    
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})"
    );
    
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(\\d{1,2}:\\d{2}(?::\\d{2})?)"
    );

    public static ReceiptData parse(String rawText) {
        ReceiptData data = new ReceiptData();
        
        // Clean text
        String text = rawText.toLowerCase();
        
        // Extract amount
        data.amount = extractAmount(text, rawText);
        
        // Extract merchant (usually first line)
        data.merchant = extractMerchant(rawText);
        
        // Extract date
        data.date = extractDate(rawText);
        
        return data;
    }
    
    private static double extractAmount(String lowerText, String rawText) {
        // Try pattern with keywords first
        Matcher matcher = AMOUNT_PATTERN.matcher(lowerText);
        if (matcher.find()) {
            return parseNumber(matcher.group(1));
        }
        
        // Try VND pattern
        matcher = AMOUNT_VND_PATTERN.matcher(lowerText);
        double maxAmount = 0;
        while (matcher.find()) {
            double amount = parseNumber(matcher.group(1));
            if (amount > maxAmount) {
                maxAmount = amount;
            }
        }
        if (maxAmount > 0) {
            return maxAmount;
        }
        
        // Try to find largest number (likely the total)
        matcher = LARGE_AMOUNT_PATTERN.matcher(rawText);
        while (matcher.find()) {
            double amount = parseNumber(matcher.group(1));
            if (amount > maxAmount && amount < 1_000_000_000) { // Sanity check
                maxAmount = amount;
            }
        }
        
        return maxAmount;
    }
    
    private static String extractMerchant(String rawText) {
        String[] lines = rawText.split("\\n");
        for (String line : lines) {
            line = line.trim();
            // Skip empty lines and lines that look like addresses/phone numbers
            if (line.isEmpty()) continue;
            if (line.matches(".*\\d{8,}.*")) continue; // Phone numbers
            if (line.toLowerCase().contains("địa chỉ")) continue;
            if (line.toLowerCase().contains("đt:")) continue;
            
            // First meaningful line is likely the merchant name
            if (line.length() > 3 && line.length() < 100) {
                return line;
            }
        }
        return null;
    }
    
    private static String extractDate(String rawText) {
        Matcher matcher = DATE_PATTERN.matcher(rawText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private static double parseNumber(String numberStr) {
        if (numberStr == null || numberStr.isEmpty()) {
            return 0;
        }
        
        // Remove spaces and normalize separators
        numberStr = numberStr.replaceAll("\\s", "");
        
        // Vietnamese format: 1.000.000 or 1,000,000
        // Remove thousand separators
        if (numberStr.contains(".") && numberStr.contains(",")) {
            // Mixed separators - assume . is thousand, , is decimal
            numberStr = numberStr.replace(".", "").replace(",", ".");
        } else if (numberStr.lastIndexOf(".") > numberStr.lastIndexOf(",")) {
            // Last separator is . - likely decimal point
            numberStr = numberStr.replace(",", "");
        } else {
            // Last separator is , or no separator - Vietnamese format
            numberStr = numberStr.replace(".", "").replace(",", ".");
        }
        
        try {
            return Double.parseDouble(numberStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static class ReceiptData {
        private double amount;
        private String merchant;
        private String date;
        
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        
        public String getMerchant() { return merchant; }
        public void setMerchant(String merchant) { this.merchant = merchant; }
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        // Compatibility alias
        public String getStringDate() { return date; }
        
        public String getFormattedAmount() {
            if (amount <= 0) {
                return "Không xác định";
            }
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            return formatter.format(amount) + " ₫";
        }
    }
}
