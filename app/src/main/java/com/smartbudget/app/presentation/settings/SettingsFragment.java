package com.smartbudget.app.presentation.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.smartbudget.app.databinding.FragmentSettingsBinding;
import com.smartbudget.app.presentation.chat.ChatActivity;
import com.smartbudget.app.presentation.scan.ScanReceiptActivity;
import com.smartbudget.app.utils.FirebaseSyncHelper;
import com.smartbudget.app.utils.ReminderReceiver;
import com.smartbudget.app.utils.GeminiHelper;
import androidx.appcompat.app.AlertDialog;
import java.util.List;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private FirebaseSyncHelper syncHelper;
    private SettingsViewModel viewModel;

    // Initialize Import Launcher
    private final androidx.activity.result.ActivityResultLauncher<Intent> importLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    android.net.Uri uri = result.getData().getData();
                    if (uri != null) {
                        // Call actual CSV import
                        com.smartbudget.app.utils.CsvImporter.quickImport(requireContext(), uri);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        syncHelper = new FirebaseSyncHelper(requireContext());
        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(SettingsViewModel.class);
        
        setupInitialState();
        setupListeners();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        updateAuthUI();
    }

        // Update Auth UI based on user state
    // Update Auth UI based on user state
    private void updateAuthUI() {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (binding == null) return;

        if (user != null) {
            // User Logged In
            binding.layoutGuest.setVisibility(View.GONE);
            binding.layoutUser.setVisibility(View.VISIBLE);
            binding.tvUserEmail.setText(user.getEmail()); // Keep displaying user email
            binding.tvSyncStatus.setText(com.smartbudget.app.R.string.sync_synced);
            binding.tvSyncStatus.setTextColor(getResources().getColor(com.smartbudget.app.R.color.success));
            binding.cardProfile.setVisibility(android.view.View.VISIBLE);
            binding.btnLogin.setVisibility(android.view.View.GONE);
            binding.btnLogout.setVisibility(android.view.View.VISIBLE);
        } else {
            // Guest
            binding.layoutGuest.setVisibility(View.VISIBLE);
            binding.layoutUser.setVisibility(View.GONE);
            binding.tvSyncStatus.setText(com.smartbudget.app.R.string.sync_not_synced);
            binding.tvSyncStatus.setTextColor(getResources().getColor(com.smartbudget.app.R.color.text_secondary));
            binding.cardProfile.setVisibility(android.view.View.VISIBLE); // Card should be visible to show guest/user layout inside
            binding.btnLogin.setVisibility(android.view.View.VISIBLE);
            binding.btnLogout.setVisibility(android.view.View.GONE);
        }
    }

    private void setupInitialState() {
        if (binding == null) return;
        
        if (binding.switchDarkMode != null) {
             binding.switchDarkMode.setChecked(com.smartbudget.app.utils.ThemeManager.getStoredTheme(requireContext()) == com.smartbudget.app.utils.ThemeManager.THEME_DARK);
        }
        
        if (binding.switchReminder != null) {
            boolean isEnabled = com.smartbudget.app.utils.ReminderReceiver.isReminderEnabled(requireContext());
            binding.switchReminder.setChecked(isEnabled);
            
            // Update subtitle with saved time
            int hour = com.smartbudget.app.utils.ReminderReceiver.getReminderHour(requireContext());
            int minute = com.smartbudget.app.utils.ReminderReceiver.getReminderMinute(requireContext());
            updateReminderSubtitle(hour, minute);
        }
        
        updateModelDisplay();
    }

    private void exportCsv() {
        Toast.makeText(requireContext(), "Đang xuất dữ liệu CSV...", Toast.LENGTH_SHORT).show();
        
        // Observe expenses and categories to build the export
        viewModel.getAllExpenses().observe(getViewLifecycleOwner(), expenses -> {
            if (expenses == null || expenses.isEmpty()) {
                Toast.makeText(requireContext(), "Không có dữ liệu để xuất", Toast.LENGTH_SHORT).show();
                return;
            }
            
            viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
                if (categories == null) return;
                
                // Build category map
                java.util.Map<Long, com.smartbudget.app.data.local.entity.CategoryEntity> categoryMap = new java.util.HashMap<>();
                for (com.smartbudget.app.data.local.entity.CategoryEntity cat : categories) {
                    categoryMap.put(cat.getId(), cat);
                }
                
                // Call CsvExporter
                try {
                    com.smartbudget.app.utils.CsvExporter.exportDataToCsv(requireContext(), expenses, categoryMap);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Lỗi xuất CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                
                // Remove observers after export to avoid multiple calls
                viewModel.getAllExpenses().removeObservers(getViewLifecycleOwner());
                viewModel.getAllCategories().removeObservers(getViewLifecycleOwner());
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Permission granted, automatically enable reminder
                if (binding != null && binding.switchReminder != null) {
                    binding.switchReminder.setChecked(true);
                }
            } else {
                Toast.makeText(requireContext(), "Cần quyền thông báo để nhắc nhở", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupListeners() {
        // ... (Theme and Reminder listeners remain same) ...
        
        // Dark mode toggle - REAL implementation
        com.smartbudget.app.utils.ThemeManager themeManager = new com.smartbudget.app.utils.ThemeManager(); // Static methods used, instance not needed but kept for structure if changed
        binding.switchDarkMode.setChecked(com.smartbudget.app.utils.ThemeManager.getStoredTheme(requireContext()) == com.smartbudget.app.utils.ThemeManager.THEME_DARK);
        
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int theme = isChecked ? com.smartbudget.app.utils.ThemeManager.THEME_DARK : com.smartbudget.app.utils.ThemeManager.THEME_LIGHT;
            com.smartbudget.app.utils.ThemeManager.saveThemePreference(requireContext(), theme);
            // Recreate activity to apply theme
            requireActivity().recreate();
        });

        // Biometric lock toggle
        com.smartbudget.app.utils.BiometricHelper biometricHelper = new com.smartbudget.app.utils.BiometricHelper(requireContext());
        
        // Initialize biometric switch state
        if (binding.switchBiometric != null) {
            binding.switchBiometric.setChecked(biometricHelper.isBiometricEnabled());
            
            // Update status text
            if (binding.tvBiometricStatus != null) {
                if (!biometricHelper.canAuthenticate()) {
                    binding.tvBiometricStatus.setText(biometricHelper.getUnavailableReason());
                    binding.switchBiometric.setEnabled(false);
                } else {
                    binding.tvBiometricStatus.setText(biometricHelper.isBiometricEnabled() ? 
                        "Đã bật khóa sinh trắc học" : "Mở khóa bằng sinh trắc học");
                }
            }
            
            binding.switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && !biometricHelper.canAuthenticate()) {
                    Toast.makeText(requireContext(), biometricHelper.getUnavailableReason(), Toast.LENGTH_LONG).show();
                    binding.switchBiometric.setChecked(false);
                    return;
                }
                
                biometricHelper.setBiometricEnabled(isChecked);
                if (binding.tvBiometricStatus != null) {
                    binding.tvBiometricStatus.setText(isChecked ? 
                        "Đã bật khóa sinh trắc học" : "Mở khóa bằng sinh trắc học");
                }
                Toast.makeText(requireContext(), 
                    isChecked ? "Đã bật khóa vân tay 🔐" : "Đã tắt khóa vân tay", 
                    Toast.LENGTH_SHORT).show();
            });
        }

        // Daily reminder toggle
        binding.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Check permission first (Android 13+)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                    androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) 
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    
                    // Request permission
                    requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                    // Turn off switch until granted (user can try again)
                    binding.switchReminder.setChecked(false);
                    return;
                }

                // If enabling, check if we have a saved time or use default
                int savedHour = ReminderReceiver.getReminderHour(requireContext());
                int savedMinute = ReminderReceiver.getReminderMinute(requireContext());
                
                // Show time picker to let user choose
                new android.app.TimePickerDialog(requireContext(), (view1, hourOfDay, minute) -> {
                    ReminderReceiver.scheduleReminder(requireContext(), hourOfDay, minute);
                    updateReminderSubtitle(hourOfDay, minute);
                    Toast.makeText(requireContext(), 
                        String.format("Đã hẹn giờ nhắc lúc %02d:%02d", hourOfDay, minute), 
                        Toast.LENGTH_SHORT).show();
                }, savedHour, savedMinute, true).show();
                
            } else {
                ReminderReceiver.cancelReminder(requireContext());
                Toast.makeText(requireContext(), "Đã tắt nhắc nhở", Toast.LENGTH_SHORT).show();
                binding.tvReminderSubtitle.setText("Nhắc nhở hàng ngày");
            }
        });
        
        // Allow clicking the text layout to change time if enabled
        binding.settingReminder.setOnClickListener(v -> {
            boolean isEnabled = binding.switchReminder.isChecked();
            if (isEnabled) {
                 int savedHour = ReminderReceiver.getReminderHour(requireContext());
                 int savedMinute = ReminderReceiver.getReminderMinute(requireContext());
                 
                 new android.app.TimePickerDialog(requireContext(), (view1, hourOfDay, minute) -> {
                    ReminderReceiver.scheduleReminder(requireContext(), hourOfDay, minute);
                    updateReminderSubtitle(hourOfDay, minute);
                    Toast.makeText(requireContext(), 
                        String.format("Đã cập nhật giờ nhắc: %02d:%02d", hourOfDay, minute), 
                        Toast.LENGTH_SHORT).show();
                }, savedHour, savedMinute, true).show();
            } else {
                // If not enabled, toggle the switch
                binding.switchReminder.setChecked(true);
            }
        });

        // --- AUTH ACTIONS ---
        
        // Login Button
        binding.btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), com.smartbudget.app.presentation.auth.LoginActivity.class));
        });

        // Logout Button
        binding.btnLogout.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Dữ liệu cục bộ sẽ bị xóa. Hãy đảm bảo đã đồng bộ dữ liệu lên cloud trước khi đăng xuất.")
                .setPositiveButton("Đăng xuất", (d, w) -> {
                    // Clear local database before signing out
                    com.smartbudget.app.data.local.AppDatabase db = 
                        com.smartbudget.app.data.local.AppDatabase.getInstance(requireContext());
                    db.clearAllData();
                    
                    // Sign out from Firebase
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                    Toast.makeText(requireContext(), "Đã đăng xuất và xóa dữ liệu cục bộ", Toast.LENGTH_SHORT).show();
                    updateAuthUI();
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
        
        // Sync with Firebase (Click to Sync manually)
        binding.settingSync.setOnClickListener(v -> {
             if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
                 // If not logged in, redirect to login
                 startActivity(new Intent(requireContext(), com.smartbudget.app.presentation.auth.LoginActivity.class));
             } else {
                 Toast.makeText(requireContext(), "Đang đồng bộ...", Toast.LENGTH_SHORT).show();
                 syncHelper.syncAll(new FirebaseSyncHelper.SyncCallback() {
                    @Override
                    public void onSuccess(String message) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
             }
        });

        // Currency setting
        binding.settingCurrency.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "Hiện chỉ hỗ trợ VND",
                    Toast.LENGTH_SHORT).show();
        });

    // Export data
        binding.settingExport.setOnClickListener(v -> {
            exportCsv();
        });

        // Backup data
        if (binding.settingBackup != null) {
            binding.settingBackup.setOnClickListener(v -> {
                com.smartbudget.app.utils.BackupManager.quickBackup(requireContext());
            });
        }


        // Import data
        if (binding.settingImport != null) {
            binding.settingImport.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                // Use broader MIME types for better compatibility
                intent.setType("*/*");
                String[] mimeTypes = {"text/csv", "text/comma-separated-values", "application/csv", "text/plain"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                importLauncher.launch(intent);
            });
        }

        // Recurring expenses
        if (binding.settingRecurring != null) {
            binding.settingRecurring.setOnClickListener(v -> {
                androidx.navigation.Navigation.findNavController(requireView())
                    .navigate(com.smartbudget.app.R.id.recurringExpenseFragment);
            });
        }



        // AI Chat - access via scan button
        if (binding.settingScanReceipt != null) {
            binding.settingScanReceipt.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), ScanReceiptActivity.class));
            });
        }

        // AI Chat
        if (binding.settingAiChat != null) {
            binding.settingAiChat.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), ChatActivity.class));
            });
        }

        // AI Model Selection
        if (binding.settingAiModel != null) {
            binding.settingAiModel.setOnClickListener(v -> {
                showModelSelectionDialog();
            });
        }

        // Savings Goals
        if (binding.settingSavingsGoals != null) {
            binding.settingSavingsGoals.setOnClickListener(v -> {
                androidx.navigation.Navigation.findNavController(requireView())
                    .navigate(com.smartbudget.app.R.id.savingsGoalFragment);
            });
        }
    }

    private void showModelSelectionDialog() {
        com.smartbudget.app.ai.AIProviderManager manager = 
            com.smartbudget.app.ai.AIProviderManager.getInstance(requireContext());
        
        String[] models = manager.getAvailableModels();
        String currentModel = manager.getCurrentModel();
        
        int selectedIndex = 0;
        for (int i = 0; i < models.length; i++) {
            if (models[i].equals(currentModel)) {
                selectedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn Model AI 🧠")
                .setSingleChoiceItems(models, selectedIndex, (dialog, which) -> {
                    // Model selection - currently just display
                    updateModelDisplay();
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Đang dùng: " + models[which], Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void updateModelDisplay() {
        com.smartbudget.app.ai.AIProviderManager manager = 
            com.smartbudget.app.ai.AIProviderManager.getInstance(requireContext());
        String name = manager.getCurrentModel();
        if (binding != null && binding.tvAiModel != null) {
            binding.tvAiModel.setText(name);
        }
    }

    private void updateReminderSubtitle(int hour, int minute) {
        if (binding != null && binding.tvReminderSubtitle != null) {
            String timeStr = String.format("%02d:%02d", hour, minute);
            binding.tvReminderSubtitle.setText("Nhắc nhở hàng ngày (" + timeStr + ")");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

