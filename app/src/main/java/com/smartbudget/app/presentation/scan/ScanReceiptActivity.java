package com.smartbudget.app.presentation.scan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.smartbudget.app.R;
import com.smartbudget.app.databinding.ActivityScanReceiptBinding;
import com.smartbudget.app.utils.ReceiptParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for scanning and processing receipt images.
 * Uses ML Kit for text recognition and extracts amount, merchant, and date.
 * 
 * <p>Refactored to use modern ActivityResultLauncher instead of deprecated
 * startActivityForResult API.</p>
 * 
 * @author SmartBudget Development Team
 * @version 2.0 - ActivityResultLauncher migration
 */
public class ScanReceiptActivity extends AppCompatActivity {

    private static final String TAG = "ScanReceiptActivity";
    private static final int PERMISSION_REQUEST = 200;

    private ActivityScanReceiptBinding binding;
    private TextRecognizer textRecognizer;
    private Uri currentPhotoUri;

    // ==================== ACTIVITY RESULT LAUNCHERS ====================
    
    /**
     * Launcher for camera capture result.
     * Replaces deprecated startActivityForResult(REQUEST_CAMERA).
     */
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && currentPhotoUri != null) {
                    processImage(currentPhotoUri);
                }
            }
    );

    /**
     * Launcher for gallery pick result.
     * Replaces deprecated startActivityForResult(REQUEST_GALLERY).
     */
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        processImage(imageUri);
                    }
                }
            }
    );

    // ==================== LIFECYCLE ====================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanReceiptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        setupListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textRecognizer != null) {
            textRecognizer.close();
        }
    }

    // ==================== UI SETUP ====================

    /**
     * Thiết lập các event listener cho các nút bấm.
     */
    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnCamera.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openCamera();
            }
        });

        binding.btnGallery.setOnClickListener(v -> openGallery());

        binding.btnConfirm.setOnClickListener(v -> confirmAndReturn());
    }

    // ==================== PERMISSION HANDLING ====================

    /**
     * Kiểm tra và yêu cầu quyền camera nếu chưa được cấp.
     * 
     * @return true nếu đã có quyền, false nếu cần yêu cầu
     */
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, R.string.scan_error_camera_permission, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ==================== CAMERA/GALLERY ====================

    /**
     * Mở camera để chụp ảnh hóa đơn.
     * Sử dụng ActivityResultLauncher thay vì startActivityForResult.
     */
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                try {
                    currentPhotoUri = FileProvider.getUriForFile(this,
                            getPackageName() + ".fileprovider", photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                    cameraLauncher.launch(intent);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error creating photo URI", e);
                    showError(getString(R.string.error_occurred));
                }
            }
        }
    }

    /**
     * Mở thư viện ảnh để chọn hóa đơn.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    /**
     * Tạo file tạm để lưu ảnh từ camera.
     * 
     * @return File hoặc null nếu có lỗi
     */
    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "RECEIPT_" + timeStamp;
        File storageDir = getExternalFilesDir("receipts");
        
        try {
            return File.createTempFile(fileName, ".jpg", storageDir);
        } catch (IOException e) {
            Log.e(TAG, "Error creating image file", e);
            return null;
        }
    }

    // ==================== IMAGE PROCESSING ====================

    /**
     * Xử lý ảnh đã chọn/chụp và trích xuất văn bản.
     * 
     * @param imageUri URI của ảnh cần xử lý
     */
    private void processImage(Uri imageUri) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.cardResult.setVisibility(View.GONE);

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                showError(getString(R.string.error_occurred));
                return;
            }
            
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            binding.ivReceipt.setImageBitmap(bitmap);
            binding.ivReceipt.setVisibility(View.VISIBLE);

            InputImage image = InputImage.fromBitmap(bitmap, 0);
            recognizeText(image);
        } catch (IOException e) {
            Log.e(TAG, "Error processing image", e);
            showError(getString(R.string.error_occurred));
        }
    }

    /**
     * Nhận dạng văn bản trong ảnh sử dụng ML Kit.
     * 
     * @param image InputImage để xử lý
     */
    private void recognizeText(InputImage image) {
        textRecognizer.process(image)
                .addOnSuccessListener(text -> {
                    String rawText = text.getText();
                    if (rawText.isEmpty()) {
                        binding.progressBar.setVisibility(View.GONE);
                        showError(getString(R.string.scan_error_no_text));
                        return;
                    }

                    // Use AI to parse receipt
                    com.smartbudget.app.ai.AIService aiService = new com.smartbudget.app.ai.impl.GroqServiceImpl();
                    if (!aiService.isConfigured()) {
                         // Fallback to Gemini if Groq not ready, or just use regex as safe layout
                         aiService = new com.smartbudget.app.ai.impl.GeminiServiceImpl();
                    }
                    
                    if (!aiService.isConfigured()) {
                         // Fallback to Regex if no AI configured
                         ReceiptParser.ReceiptData receiptData = ReceiptParser.parse(rawText);
                         binding.progressBar.setVisibility(View.GONE);
                         showResult(receiptData, rawText);
                         return;
                    }

                    aiService.parseReceipt(rawText, new com.smartbudget.app.ai.AICallback() {
                        @Override
                        public void onSuccess(String response) {
                            binding.progressBar.setVisibility(View.GONE);
                            try {
                                org.json.JSONObject json = new org.json.JSONObject(response);
                                ReceiptParser.ReceiptData data = new ReceiptParser.ReceiptData();
                                
                                // Extract data safely
                                data.setAmount(json.optDouble("amount", 0));
                                data.setMerchant(json.optString("merchant", getString(R.string.unknown)));
                                data.setDate(json.optString("date", getString(R.string.today)));
                                
                                // Use items as description/note if available
                                org.json.JSONArray items = json.optJSONArray("items");
                                StringBuilder note = new StringBuilder();
                                if (items != null) {
                                    for (int i = 0; i < items.length(); i++) {
                                        note.append("- ").append(items.optString(i)).append("\n");
                                    }
                                }
                                
                                // Store items in merchant field temporarily using a separator or just strict usage? 
                                // Actually, ReceiptData class might need update, OR we pass it via Intent extra differently.
                                // For now, let's append items to merchant name for visibility? No, bad UX.
                                // Let's put it in a hidden tag or just pass purely via Intent.
                                
                                binding.btnConfirm.setTag(new Bundle()); // Use Bundle to store complex data
                                ((Bundle)binding.btnConfirm.getTag()).putDouble("amount", data.getAmount());
                                ((Bundle)binding.btnConfirm.getTag()).putString("merchant", data.getMerchant());
                                ((Bundle)binding.btnConfirm.getTag()).putString("date", data.getDate());
                                ((Bundle)binding.btnConfirm.getTag()).putString("note", note.toString());

                                showResult(data, rawText);
                                
                            } catch (org.json.JSONException e) {
                                Log.e(TAG, "JSON Parse Error", e);
                                // Fallback to Regex
                                ReceiptParser.ReceiptData fallbackData = ReceiptParser.parse(rawText);
                                showResult(fallbackData, rawText);
                            }
                        }

                        @Override
                        public void onError(String error, int code) {
                             Log.e(TAG, "AI Error: " + error);
                             binding.progressBar.setVisibility(View.GONE);
                             // Fallback to Regex
                             ReceiptParser.ReceiptData fallbackData = ReceiptParser.parse(rawText);
                             showResult(fallbackData, rawText);
                             Toast.makeText(ScanReceiptActivity.this, "AI failed, using basic scan: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Text recognition failed", e);
                    binding.progressBar.setVisibility(View.GONE);
                    showError(getString(R.string.scan_error_recognition_prefix) + e.getMessage());
                });
    }

    // ==================== RESULT HANDLING ====================

    /**
     * Hiển thị kết quả phân tích hóa đơn.
     * 
     * @param data Dữ liệu đã phân tích
     * @param rawText Văn bản gốc
     */
    private void showResult(ReceiptParser.ReceiptData data, String rawText) {
        binding.cardResult.setVisibility(View.VISIBLE);
        
        // Display parsed data
        binding.tvAmount.setText(data.getFormattedAmount());
        binding.tvMerchant.setText(data.getMerchant() != null ? data.getMerchant() : getString(R.string.unknown));
        binding.tvDate.setText(data.getDate() != null ? data.getDate() : getString(R.string.today));
        
        // Show raw text for reference
        binding.tvRawText.setText(rawText);
        
        // Store data for returning
        binding.btnConfirm.setTag(data);
    }

    /**
     * Xác nhận và trả về dữ liệu cho Activity gọi.
     */
    private void confirmAndReturn() {
        Object tag = binding.btnConfirm.getTag();
        if (tag instanceof Bundle) {
            Bundle data = (Bundle) tag;
            Intent resultIntent = new Intent();
            resultIntent.putExtra("amount", data.getDouble("amount"));
            resultIntent.putExtra("merchant", data.getString("merchant"));
            resultIntent.putExtra("date", data.getString("date")); // Note: date format might need parsing in Dashboard
            resultIntent.putExtra("note", data.getString("note")); // Pass extracted items as note

            // Parse date string to long if possible
            try {
                // Assuming format DD/MM/YYYY or YYYY-MM-DD
                // SimpleDateFormat logic omitted for brevity, basic long passing if failed
            } catch (Exception e) {
                // Ignore
            }

            setResult(RESULT_OK, resultIntent);
            finish();
        } else if (tag instanceof ReceiptParser.ReceiptData) {
            // Legacy fallback
            ReceiptParser.ReceiptData data = (ReceiptParser.ReceiptData) tag;
            
            Intent resultIntent = new Intent();
            resultIntent.putExtra("amount", data.getAmount());
            resultIntent.putExtra("merchant", data.getMerchant());
            resultIntent.putExtra("date", data.getStringDate()); // Use string date
            
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, R.string.scan_error_no_image, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Hiển thị thông báo lỗi với các tùy chọn khắc phục.
     * 
     * @param message Nội dung lỗi
     */
    private void showError(String message) {
        binding.progressBar.setVisibility(View.GONE);
        
        // Show error dialog with options
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("❌ Lỗi quét hóa đơn")
            .setMessage(message + "\n\nBạn muốn làm gì tiếp?")
            .setPositiveButton("🔄 Thử lại", (dialog, which) -> {
                // Reset and let user take new photo
                binding.ivReceipt.setVisibility(View.GONE);
                binding.cardResult.setVisibility(View.GONE);
            })
            .setNegativeButton("✍️ Nhập thủ công", (dialog, which) -> {
                // Return to previous screen with empty data - let user manually enter
                Intent resultIntent = new Intent();
                resultIntent.putExtra("manual_entry", true);
                resultIntent.putExtra("amount", 0.0);
                resultIntent.putExtra("merchant", "");
                resultIntent.putExtra("note", "");
                setResult(RESULT_OK, resultIntent);
                finish();
            })
            .setNeutralButton("Hủy", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }
}
