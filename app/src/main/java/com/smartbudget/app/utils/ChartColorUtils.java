package com.smartbudget.app.utils;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.smartbudget.app.R;

/**
 * Utility class for centralized chart and UI colors.
 * Eliminates DRY violation by providing single source of truth for color arrays.
 * 
 * <h3>Usage:</h3>
 * <pre>
 * // Get chart colors array
 * int[] colors = ChartColorUtils.getChartColors(context);
 * 
 * // Get specific semantic color
 * int expenseColor = ChartColorUtils.getExpenseColor(context);
 * </pre>
 * 
 * @author SmartBudget Development Team
 * @version 1.0
 */
public final class ChartColorUtils {

    // Private constructor to prevent instantiation
    private ChartColorUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== CHART COLOR ARRAYS ====================

    /**
     * Gets the standard chart color palette (8 colors).
     * Used for pie charts, bar charts, and category visualizations.
     *
     * @param context Android context for resource access
     * @return Array of color integers
     */
    @NonNull
    @ColorInt
    public static int[] getChartColors(@NonNull Context context) {
        return new int[] {
            android.graphics.Color.parseColor("#FF6B6B"), // Coral Red
            android.graphics.Color.parseColor("#4ECDC4"), // Caribbean Green
            android.graphics.Color.parseColor("#45B7D1"), // Sky Blue
            android.graphics.Color.parseColor("#96CEB4"), // Pale Green
            android.graphics.Color.parseColor("#FFEEAD"), // Cream Yellow
            android.graphics.Color.parseColor("#D4A5A5"), // Dusty Pink
            android.graphics.Color.parseColor("#9B59B6"), // Amethyst
            android.graphics.Color.parseColor("#34495E")  // Wet Asphalt
        };
    }

    /**
     * Gets a color from the chart palette by index (with wraparound).
     *
     * @param context Android context
     * @param index Color index (will wrap around if > 7)
     * @return Color integer
     */
    @ColorInt
    public static int getChartColorAt(@NonNull Context context, int index) {
        int[] colors = getChartColors(context);
        return colors[Math.abs(index) % colors.length];
    }

    // ==================== SEMANTIC COLORS ====================

    /**
     * Gets the expense (negative) color - Red.
     * Use for expense amounts, over-budget indicators.
     *
     * @param context Android context
     * @return Expense color integer
     */
    @ColorInt
    public static int getExpenseColor(@NonNull Context context) {
        return ContextCompat.getColor(context, R.color.expense_color);
    }

    /**
     * Gets the income (positive) color - Green.
     * Use for income amounts, savings indicators.
     *
     * @param context Android context
     * @return Income color integer
     */
    @ColorInt
    public static int getIncomeColor(@NonNull Context context) {
        return ContextCompat.getColor(context, R.color.income_color);
    }

    /**
     * Gets the warning color - Orange/Yellow.
     * Use for budget warnings, alerts.
     *
     * @param context Android context
     * @return Warning color integer
     */
    @ColorInt
    public static int getWarningColor(@NonNull Context context) {
        return ContextCompat.getColor(context, R.color.warning_color);
    }

    /**
     * Gets the primary theme color.
     *
     * @param context Android context
     * @return Primary color integer
     */
    @ColorInt
    public static int getPrimaryColor(@NonNull Context context) {
        return ContextCompat.getColor(context, R.color.primary);
    }

    // ==================== TEXT COLORS ====================

    /**
     * Gets the primary text color (dark).
     *
     * @param context Android context
     * @return Text primary color integer
     */
    @ColorInt
    public static int getTextPrimaryColor(@NonNull Context context) {
        return ContextCompat.getColor(context, R.color.text_primary);
    }

    /**
     * Gets the secondary text color (gray).
     *
     * @param context Android context
     * @return Text secondary color integer
     */
    @ColorInt
    public static int getTextSecondaryColor(@NonNull Context context) {
        return ContextCompat.getColor(context, R.color.text_secondary);
    }

    /**
     * Gets the hint text color (light gray).
     *
     * @param context Android context
     * @return Text hint color integer
     */
    @ColorInt
    public static int getTextHintColor(@NonNull Context context) {
        return ContextCompat.getColor(context, R.color.text_hint);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Applies alpha transparency to a color.
     * Useful for creating semi-transparent backgrounds.
     *
     * @param color Original color
     * @param alpha Alpha value (0-255), where 0 is fully transparent
     * @return Color with applied alpha
     */
    @ColorInt
    public static int withAlpha(@ColorInt int color, int alpha) {
        return (color & 0x00FFFFFF) | (Math.min(255, Math.max(0, alpha)) << 24);
    }

    /**
     * Creates a semi-transparent version of a color (33% opacity).
     * Used for icon backgrounds, highlights.
     *
     * @param color Original color
     * @return Color with ~33% opacity
     */
    @ColorInt
    public static int withLowAlpha(@ColorInt int color) {
        return withAlpha(color, 85);  // ~33% of 255
    }

    /**
     * Parses a color from category entity safely.
     * Falls back to default gray if parsing fails.
     *
     * @param colorString Hex color string (e.g., "#FF6B6B")
     * @param context Android context for fallback color
     * @return Parsed color or default
     */
    @ColorInt
    public static int parseColorSafe(String colorString, @NonNull Context context) {
        if (colorString == null || colorString.isEmpty()) {
            return ContextCompat.getColor(context, R.color.cat_other);
        }
        
        try {
            return android.graphics.Color.parseColor(colorString);
        } catch (IllegalArgumentException e) {
            return ContextCompat.getColor(context, R.color.cat_other);
        }
    }
}
