package com.smartbudget.app.presentation.onboarding;

public class OnboardingItem {
    private String emoji;
    private String title;
    private String description;

    public OnboardingItem(String emoji, String title, String description) {
        this.emoji = emoji;
        this.title = title;
        this.description = description;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
