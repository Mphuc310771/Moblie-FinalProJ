package com.smartbudget.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Theme customization helper.
 * Allows users to personalize their app appearance.
 */
public class ThemeHelper {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "selected_theme";
    private static final String KEY_ACCENT_COLOR = "accent_color";

    public enum AppTheme {
        LIGHT("light", "☀️ Sáng"),
        DARK("dark", "🌙 Tối"),
        SYSTEM("system", "📱 Theo hệ thống"),
        AMOLED("amoled", "⬛ AMOLED");

        public final String id;
        public final String displayName;

        AppTheme(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
    }

    public enum AccentColor {
        GREEN("#2E7D32", "🌿 Xanh lá"),
        BLUE("#1976D2", "💙 Xanh dương"),
        PURPLE("#7B1FA2", "💜 Tím"),
        ORANGE("#F57C00", "🧡 Cam"),
        PINK("#C2185B", "💗 Hồng"),
        TEAL("#00796B", "🩵 Xanh ngọc");

        public final String colorHex;
        public final String displayName;

        AccentColor(String colorHex, String displayName) {
            this.colorHex = colorHex;
            this.displayName = displayName;
        }
    }

    /**
     * Get current theme.
     */
    public static AppTheme getCurrentTheme(Context context) {
        String themeId = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_THEME, AppTheme.SYSTEM.id);

        for (AppTheme theme : AppTheme.values()) {
            if (theme.id.equals(themeId)) {
                return theme;
            }
        }
        return AppTheme.SYSTEM;
    }

    /**
     * Set theme.
     */
    public static void setTheme(Context context, AppTheme theme) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_THEME, theme.id)
                .apply();
    }

    /**
     * Get current accent color.
     */
    public static AccentColor getCurrentAccentColor(Context context) {
        String colorHex = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ACCENT_COLOR, AccentColor.GREEN.colorHex);

        for (AccentColor color : AccentColor.values()) {
            if (color.colorHex.equals(colorHex)) {
                return color;
            }
        }
        return AccentColor.GREEN;
    }

    /**
     * Set accent color.
     */
    public static void setAccentColor(Context context, AccentColor color) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ACCENT_COLOR, color.colorHex)
                .apply();
    }

    /**
     * Apply theme to app.
     * Call in Application.onCreate() or Activity.onCreate().
     */
    public static void applyTheme(Context context) {
        AppTheme theme = getCurrentTheme(context);
        switch (theme) {
            case LIGHT:
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case DARK:
            case AMOLED:
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case SYSTEM:
            default:
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    /**
     * Check if using dark/AMOLED theme.
     */
    public static boolean isDarkMode(Context context) {
        AppTheme theme = getCurrentTheme(context);
        return theme == AppTheme.DARK || theme == AppTheme.AMOLED;
    }
}
