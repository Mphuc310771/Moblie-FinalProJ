# SmartBudget 💰

Ứng dụng quản lý chi tiêu cá nhân thông minh trên Android với tích hợp AI.

## 📱 Tính năng chính

### 💵 Quản lý Thu Chi
- Thêm, sửa, xóa giao dịch thu/chi
- Phân loại theo danh mục (Ăn uống, Di chuyển, Mua sắm...)
- Quét hóa đơn tự động bằng ML Kit OCR
- Hỗ trợ chi tiêu định kỳ (lương, tiền nhà...)

### 📊 Báo cáo & Thống kê
- Biểu đồ tròn theo danh mục
- Biểu đồ cột xu hướng chi tiêu
- Lọc theo Tuần/Tháng/Năm
- **Tùy chọn khoảng ngày bất kỳ**

### 💰 Ngân sách
- Thiết lập ngân sách theo danh mục
- Cảnh báo khi sắp vượt ngân sách
- Theo dõi tiến độ chi tiêu

### 🎯 Mục tiêu Tiết kiệm
- Tạo mục tiêu tiết kiệm
- Theo dõi tiến độ đạt mục tiêu
- Thêm tiền vào mục tiêu

### 🤖 Trợ lý AI
- Chat với AI về tài chính cá nhân
- Phân tích chi tiêu và đưa lời khuyên
- Hỗ trợ nhiều model: Gemini, Groq

### ☁️ Đồng bộ Cloud
- Đăng nhập Firebase Authentication
- Đồng bộ dữ liệu lên Firestore
- **Dữ liệu tách biệt giữa các tài khoản**

### 🎨 Giao diện
- Material Design 3
- Hỗ trợ Dark Mode
- Giao diện tiếng Việt

## 🛠️ Công nghệ sử dụng

| Công nghệ | Mô tả |
|-----------|-------|
| **Kotlin/Java** | Ngôn ngữ lập trình |
| **Room Database** | Lưu trữ dữ liệu local |
| **Firebase Auth** | Xác thực người dùng |
| **Firestore** | Đồng bộ dữ liệu cloud |
| **MPAndroidChart** | Biểu đồ thống kê |
| **ML Kit** | OCR quét hóa đơn |
| **Gemini AI** | Trợ lý AI thông minh |
| **Material 3** | UI Components |

## 📁 Cấu trúc Project

```
app/src/main/java/com/smartbudget/app/
├── ai/                     # AI Service (Gemini, Groq)
├── data/
│   ├── local/
│   │   ├── dao/           # Data Access Objects
│   │   └── entity/        # Room Entities
│   └── repository/        # Repositories
├── presentation/
│   ├── auth/              # Login/Register
│   ├── dashboard/         # Trang chủ
│   ├── reports/           # Báo cáo
│   ├── budget/            # Ngân sách
│   ├── savings/           # Mục tiêu tiết kiệm
│   ├── recurring/         # Chi tiêu định kỳ
│   ├── chat/              # Chat AI
│   ├── scan/              # Quét hóa đơn
│   └── settings/          # Cài đặt
└── utils/                 # Utilities
```

## 🚀 Cài đặt

1. Clone repository:
```bash
git clone https://github.com/Mphuc310771/Moblie-FinalProJ.git
```

2. Mở project bằng Android Studio

3. Thêm file `google-services.json` vào thư mục `app/`

4. Build và chạy trên thiết bị/emulator

## 📝 Yêu cầu

- Android Studio Hedgehog hoặc mới hơn
- Android SDK 24+ (Android 7.0)
- JDK 17

## 👨‍💻 Thành viên nhóm

- **Mphuc310771** - Developer

## 📄 License

MIT License - Xem file [LICENSE](LICENSE) để biết thêm chi tiết.
