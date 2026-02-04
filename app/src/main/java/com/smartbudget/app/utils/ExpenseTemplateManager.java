package com.smartbudget.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Expense template manager.
 * Save and reuse common expense patterns.
 */
public class ExpenseTemplateManager {

    private static final String PREFS_NAME = "expense_templates";
    private static final String KEY_TEMPLATES = "templates";

    public static class Template {
        public String id;
        public String name;
        public String emoji;
        public double amount;
        public String category;
        public String note;
        public int usageCount;

        public Template(String name, String emoji, double amount, String category, String note) {
            this.id = String.valueOf(System.currentTimeMillis());
            this.name = name;
            this.emoji = emoji;
            this.amount = amount;
            this.category = category;
            this.note = note;
            this.usageCount = 0;
        }
    }

    // Pre-defined templates
    public static final Template[] DEFAULT_TEMPLATES = {
            new Template("Cà phê sáng", "☕", 35000, "Ăn uống", "Cà phê"),
            new Template("Cơm trưa", "🍚", 45000, "Ăn uống", "Cơm trưa"),
            new Template("Grab về nhà", "🚗", 50000, "Di chuyển", "Grab/Taxi"),
            new Template("Trà sữa", "🧋", 40000, "Ăn uống", "Trà sữa"),
            new Template("Xăng xe", "⛽", 100000, "Di chuyển", "Đổ xăng"),
            new Template("Tiền điện", "💡", 500000, "Hóa đơn", "Tiền điện tháng"),
            new Template("Netflix", "🎬", 180000, "Giải trí", "Netflix subscription"),
            new Template("Gym", "💪", 500000, "Sức khỏe", "Phí gym tháng")
    };

    private Context context;
    private SharedPreferences prefs;
    private Gson gson;

    public ExpenseTemplateManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Get all templates (custom + default).
     */
    public List<Template> getAllTemplates() {
        List<Template> templates = getCustomTemplates();
        
        // Add defaults if no custom templates
        if (templates.isEmpty()) {
            for (Template t : DEFAULT_TEMPLATES) {
                templates.add(t);
            }
        }
        
        return templates;
    }

    /**
     * Get custom templates.
     */
    public List<Template> getCustomTemplates() {
        String json = prefs.getString(KEY_TEMPLATES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<Template>>(){}.getType();
        return gson.fromJson(json, type);
    }

    /**
     * Save a new template.
     */
    public void saveTemplate(Template template) {
        List<Template> templates = getCustomTemplates();
        templates.add(template);
        saveTemplates(templates);
    }

    /**
     * Update template usage count.
     */
    public void incrementUsage(String templateId) {
        List<Template> templates = getCustomTemplates();
        for (Template t : templates) {
            if (t.id.equals(templateId)) {
                t.usageCount++;
                break;
            }
        }
        saveTemplates(templates);
    }

    /**
     * Delete a template.
     */
    public void deleteTemplate(String templateId) {
        List<Template> templates = getCustomTemplates();
        templates.removeIf(t -> t.id.equals(templateId));
        saveTemplates(templates);
    }

    /**
     * Get most used templates.
     */
    public List<Template> getMostUsedTemplates(int limit) {
        List<Template> templates = new ArrayList<>(getCustomTemplates());
        templates.sort((a, b) -> Integer.compare(b.usageCount, a.usageCount));
        
        if (templates.size() > limit) {
            return templates.subList(0, limit);
        }
        return templates;
    }

    private void saveTemplates(List<Template> templates) {
        String json = gson.toJson(templates);
        prefs.edit().putString(KEY_TEMPLATES, json).apply();
    }
}
