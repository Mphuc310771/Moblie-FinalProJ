package com.smartbudget.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Location tag manager.
 * Tag expenses with locations for pattern analysis.
 */
public class LocationTagManager {

    private static final String PREFS_NAME = "location_tags";
    private static final String KEY_LOCATIONS = "locations";
    private static final String KEY_HISTORY = "location_history";

    public static class LocationTag {
        public String id;
        public String name;
        public String emoji;
        public String category; // Auto-suggested category for this location
        public int usageCount;
        public double averageSpend;
        public double totalSpend;

        public LocationTag(String name, String emoji) {
            this.id = String.valueOf(System.currentTimeMillis());
            this.name = name;
            this.emoji = emoji;
            this.usageCount = 0;
            this.averageSpend = 0;
            this.totalSpend = 0;
        }

        public void recordSpend(double amount) {
            usageCount++;
            totalSpend += amount;
            averageSpend = totalSpend / usageCount;
        }
    }

    // Pre-defined locations
    public static final LocationTag[] COMMON_LOCATIONS = {
            new LocationTag("Công ty", "🏢"),
            new LocationTag("Nhà", "🏠"),
            new LocationTag("Siêu thị", "🛒"),
            new LocationTag("Quán cà phê", "☕"),
            new LocationTag("Nhà hàng", "🍽️"),
            new LocationTag("Chợ", "🥬"),
            new LocationTag("Cửa hàng tiện lợi", "🏪"),
            new LocationTag("Gym", "💪"),
            new LocationTag("Bệnh viện", "🏥"),
            new LocationTag("Trường học", "🏫"),
            new LocationTag("Rạp phim", "🎬"),
            new LocationTag("Spa", "💆")
    };

    private Context context;
    private SharedPreferences prefs;
    private Gson gson;

    public LocationTagManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Get all locations (custom + default).
     */
    public List<LocationTag> getAllLocations() {
        List<LocationTag> all = new ArrayList<>();
        for (LocationTag l : COMMON_LOCATIONS) {
            all.add(l);
        }
        all.addAll(getCustomLocations());
        return all;
    }

    /**
     * Get custom locations only.
     */
    public List<LocationTag> getCustomLocations() {
        String json = prefs.getString(KEY_LOCATIONS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<LocationTag>>(){}.getType();
        return gson.fromJson(json, type);
    }

    /**
     * Add custom location.
     */
    public void addLocation(LocationTag location) {
        List<LocationTag> locations = getCustomLocations();
        locations.add(location);
        saveLocations(locations);
    }

    /**
     * Record spending at location.
     */
    public void recordSpend(String locationId, double amount) {
        List<LocationTag> locations = getCustomLocations();
        for (LocationTag l : locations) {
            if (l.id.equals(locationId)) {
                l.recordSpend(amount);
                break;
            }
        }
        saveLocations(locations);
    }

    /**
     * Get top spending locations.
     */
    public List<LocationTag> getTopSpendingLocations(int limit) {
        List<LocationTag> all = new ArrayList<>(getCustomLocations());
        all.sort((a, b) -> Double.compare(b.totalSpend, a.totalSpend));
        
        if (all.size() > limit) {
            return all.subList(0, limit);
        }
        return all;
    }

    /**
     * Get location statistics.
     */
    public Map<String, Double> getLocationStats() {
        Map<String, Double> stats = new HashMap<>();
        for (LocationTag l : getCustomLocations()) {
            stats.put(l.name, l.totalSpend);
        }
        return stats;
    }

    /**
     * Suggest category based on location.
     */
    public String suggestCategory(String locationName) {
        String lower = locationName.toLowerCase();
        
        if (lower.contains("quán") || lower.contains("nhà hàng") || lower.contains("cafe")) {
            return "Ăn uống";
        } else if (lower.contains("siêu thị") || lower.contains("chợ")) {
            return "Mua sắm";
        } else if (lower.contains("bệnh viện") || lower.contains("phòng khám")) {
            return "Y tế";
        } else if (lower.contains("rạp") || lower.contains("karaoke")) {
            return "Giải trí";
        } else if (lower.contains("gym") || lower.contains("spa")) {
            return "Sức khỏe";
        }
        
        return "Khác";
    }

    private void saveLocations(List<LocationTag> locations) {
        String json = gson.toJson(locations);
        prefs.edit().putString(KEY_LOCATIONS, json).apply();
    }
}
