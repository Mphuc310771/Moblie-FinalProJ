package com.smartbudget.app.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for date formatting and manipulation.
 * 
 * <p>THREAD-SAFETY: All formatters use ThreadLocal to ensure thread safety.
 * SimpleDateFormat is NOT thread-safe, so we create one instance per thread.</p>
 * 
 * @author SmartBudget Development Team
 * @version 2.0
 */
public class DateUtils {

    // Vietnamese locale for all formatters
    private static final Locale LOCALE_VN = new Locale("vi", "VN");

    // ==================== THREAD-SAFE FORMATTERS ====================
    
    /**
     * Thread-local SimpleDateFormat for "dd/MM/yyyy" pattern.
     * Each thread gets its own instance to avoid race conditions.
     */
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd/MM/yyyy", LOCALE_VN);
        }
    };

    /**
     * Thread-local SimpleDateFormat for "dd/MM/yyyy HH:mm" pattern.
     */
    private static final ThreadLocal<SimpleDateFormat> DATE_TIME_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm", LOCALE_VN);
        }
    };

    /**
     * Thread-local SimpleDateFormat for "MM/yyyy" pattern.
     */
    private static final ThreadLocal<SimpleDateFormat> MONTH_YEAR_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MM/yyyy", LOCALE_VN);
        }
    };

    // ==================== PUBLIC FORMATTING METHODS ====================

    /**
     * Formats a timestamp to "dd/MM/yyyy" string.
     * 
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted date string (e.g., "15/01/2026")
     */
    public static String formatDate(long timestamp) {
        return DATE_FORMAT.get().format(new Date(timestamp));
    }

    /**
     * Formats a timestamp to "dd/MM/yyyy HH:mm" string.
     * 
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted date-time string (e.g., "15/01/2026 14:30")
     */
    public static String formatDateTime(long timestamp) {
        return DATE_TIME_FORMAT.get().format(new Date(timestamp));
    }

    /**
     * Formats a timestamp to "MM/yyyy" string.
     * 
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted month-year string (e.g., "01/2026")
     */
    public static String formatMonthYear(long timestamp) {
        return MONTH_YEAR_FORMAT.get().format(new Date(timestamp));
    }

    /**
     * Gets a human-readable relative date string.
     * Returns "Hôm nay", "Hôm qua", or the formatted date.
     * 
     * @param timestamp Unix timestamp in milliseconds
     * @return Relative date string
     */
    public static String getRelativeDate(long timestamp) {
        Calendar today = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp);

        if (isSameDay(today, date)) {
            return "Hôm nay";
        }

        today.add(Calendar.DAY_OF_YEAR, -1);
        if (isSameDay(today, date)) {
            return "Hôm qua";
        }

        return formatDate(timestamp);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Checks if two Calendar instances represent the same day.
     */
    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    // ==================== DAY BOUNDARY METHODS ====================

    /**
     * Gets the start of day (00:00:00.000) for a given timestamp.
     * 
     * @param timestamp Unix timestamp in milliseconds
     * @return Timestamp at start of that day
     */
    public static long getStartOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Gets the end of day (23:59:59.999) for a given timestamp.
     * 
     * @param timestamp Unix timestamp in milliseconds
     * @return Timestamp at end of that day
     */
    public static long getEndOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    // ==================== MONTH BOUNDARY METHODS ====================

    /**
     * Gets the start of a specific month.
     * 
     * @param month Month (1-12)
     * @param year Year (e.g., 2026)
     * @return Timestamp at start of that month
     */
    public static long getStartOfMonth(int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1); // Calendar months are 0-indexed
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return getStartOfDay(cal.getTimeInMillis());
    }

    /**
     * Gets the end of a specific month.
     * 
     * @param month Month (1-12)
     * @param year Year (e.g., 2026)
     * @return Timestamp at end of that month
     */
    public static long getEndOfMonth(int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return getEndOfDay(cal.getTimeInMillis());
    }

    // ==================== CURRENT DATE METHODS ====================

    /**
     * Gets the current month (1-12).
     * 
     * @return Current month number
     */
    public static int getCurrentMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    /**
     * Gets the current year.
     * 
     * @return Current year (e.g., 2026)
     */
    public static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * Gets the current timestamp in milliseconds.
     * 
     * @return Current Unix timestamp
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
}
