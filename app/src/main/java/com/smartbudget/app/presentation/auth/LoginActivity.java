package com.smartbudget.app.presentation.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.smartbudget.app.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.btnLogin.setOnClickListener(v -> login());
        binding.tvRegister.setOnClickListener(v -> {
             startActivity(new Intent(this, RegisterActivity.class));
             finish();
        });
    }

    private void login() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        // Sync data from Firebase after login
                        com.smartbudget.app.utils.FirebaseSyncHelper syncHelper = 
                            new com.smartbudget.app.utils.FirebaseSyncHelper(this);
                        syncHelper.downloadData(new com.smartbudget.app.utils.FirebaseSyncHelper.SyncCallback() {
                            @Override
                            public void onSuccess(String message) {
                                runOnUiThread(() -> {
                                    Toast.makeText(LoginActivity.this, 
                                        "Đăng nhập thành công! " + message, Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }
                            
                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    Toast.makeText(LoginActivity.this, 
                                        "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }
                        });
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
