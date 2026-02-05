package com.smartbudget.app.presentation.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.smartbudget.app.R;
import com.smartbudget.app.databinding.ActivityOnboardingBinding;
import com.smartbudget.app.presentation.auth.LoginActivity;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;
    private OnboardingAdapter onboardingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupOnboardingItems();
        setupViewPager();
        setupListeners();
    }

    private void setupOnboardingItems() {
        List<OnboardingItem> onboardingItems = new ArrayList<>();

        onboardingItems.add(new OnboardingItem(
                "💰",
                "Quản lý Chi tiêu Thông minh",
                "Theo dõi thu chi hàng ngày dễ dàng. Phân loại chi phí và kiểm soát ngân sách hiệu quả."
        ));

        onboardingItems.add(new OnboardingItem(
                "📊",
                "Báo cáo & Biểu đồ",
                "Trực quan hóa dữ liệu tài chính của bạn với các biểu đồ chi tiết. Hiểu rõ dòng tiền của bạn đi đâu."
        ));

        onboardingItems.add(new OnboardingItem(
                "🤖",
                "Trợ lý AI Hỗ trợ",
                "Chat với AI để nhận lời khuyên tài chính, phân tích chi tiêu và giải đáp thắc mắc."
        ));

        onboardingItems.add(new OnboardingItem(
                "☁️",
                "Đồng bộ Đám mây",
                "Dữ liệu được lưu trữ an toàn và đồng bộ trên các thiết bị. Không bao giờ mất dữ liệu."
        ));

        onboardingAdapter = new OnboardingAdapter(onboardingItems);
    }

    private void setupViewPager() {
        binding.viewPager.setAdapter(onboardingAdapter);
        setupIndicators(onboardingAdapter.getItemCount());
        setCurrentIndicator(0);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);
                
                // Update Button Text Logic
                if (position == onboardingAdapter.getItemCount() - 1) {
                    binding.btnNext.setText("Bắt đầu");
                } else {
                    binding.btnNext.setText("Tiếp tục");
                }
            }
        });
    }

    private void setupIndicators(int count) {
        // Indicators are already in XML (dot_1 to dot_4), but if we want dynamic:
        // For simplicity and matching XML, we assume 4 items max or update manually.
        // XML has 4 dots hardcoded.
    }

    private void setCurrentIndicator(int position) {
        int childCount = binding.indicatorContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View indicator = binding.indicatorContainer.getChildAt(i);
            if (i == position) {
                indicator.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_dot_active));
            } else {
                indicator.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_dot_inactive));
            }
        }
    }

    private void setupListeners() {
        binding.btnNext.setOnClickListener(v -> {
            if (binding.viewPager.getCurrentItem() + 1 < onboardingAdapter.getItemCount()) {
                binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
            } else {
                completeOnboarding();
            }
        });

        binding.btnSkip.setOnClickListener(v -> completeOnboarding());
    }

    private void completeOnboarding() {
        // Save state
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("onboarding_completed", true);
        editor.apply();

        // Navigate to Login/Home
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
