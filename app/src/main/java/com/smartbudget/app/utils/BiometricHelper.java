package com.smartbudget.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

/**
 * Helper class for biometric authentication.
 * Provides fingerprint/face unlock functionality for app security.
 */
public class BiometricHelper {

    private static final String PREFS_NAME = "biometric_prefs";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";

    private final Context context;
    private final SharedPreferences prefs;

    public BiometricHelper(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Check if biometric authentication is available on this device.
     * @return true if device supports biometric auth and has enrolled biometrics
     */
    public boolean canAuthenticate() {
        BiometricManager biometricManager = BiometricManager.from(context);
        int result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
        return result == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * Get a user-friendly message explaining why biometric is not available.
     */
    public String getUnavailableReason() {
        BiometricManager biometricManager = BiometricManager.from(context);
        int result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
        
        switch (result) {
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return "Thiết bị không hỗ trợ sinh trắc học";
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return "Sinh trắc học tạm thời không khả dụng";
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return "Chưa đăng ký vân tay/khuôn mặt. Vui lòng đăng ký trong Cài đặt hệ thống";
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                return "Cần cập nhật bảo mật";
            default:
                return "Không thể sử dụng sinh trắc học";
        }
    }

    /**
     * Check if user has enabled biometric lock for this app.
     */
    public boolean isBiometricEnabled() {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    /**
     * Enable or disable biometric lock for this app.
     */
    public void setBiometricEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
    }

    /**
     * Show biometric authentication prompt.
     * 
     * @param activity The FragmentActivity to show the prompt in
     * @param callback Callback for authentication result
     */
    public void authenticate(FragmentActivity activity, AuthCallback callback) {
        if (!canAuthenticate()) {
            callback.onError(getUnavailableReason());
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(activity);

        BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        // Error code 10 = user canceled, 13 = negative button
                        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            callback.onUsePassword();
                        } else if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                            callback.onError(errString.toString());
                        }
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        callback.onSuccess();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        // Don't callback here - user can try again
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Mở khóa SmartBudget 🔐")
                .setSubtitle("Xác thực để truy cập ứng dụng")
                .setNegativeButtonText("Sử dụng mật khẩu")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * Callback interface for biometric authentication results.
     */
    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
        void onUsePassword(); // User chose to use password instead
    }
}
