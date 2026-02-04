# SmartBudget

Ứng dụng quản lý tài chính cá nhân dành cho nền tảng Android, tích hợp Trí tuệ nhân tạo (AI) và công nghệ nhận diện quang học (OCR).

## Tổng Quan

SmartBudget cung cấp giải pháp theo dõi thu chi toàn diện, giúp người dùng quản lý ngân sách hiệu quả thông qua giao diện trực quan và các công cụ tự động hóa.

## Tính Năng Chính

### 1. Quản Lý Thu Chi
*   **Ghi chép giao dịch:** Thêm, sửa, xóa các khoản thu/chi theo danh mục.
*   **Ngân sách:** Thiết lập hạn mức chi tiêu cho từng danh mục và nhận cảnh báo khi vượt mức.
*   **Tiết kiệm:** Theo dõi tiến độ tích lũy cho các mục tiêu tài chính cụ thể.

### 2. Tự Động Hóa & AI
*   **OCR (Optical Character Recognition):** Quét và trích xuất thông tin (tổng tiền, ngày tháng, nội dung) từ hóa đơn giấy bằng Google ML Kit Vision.
*   **Trợ lý ảo:** Tích hợp mô hình ngôn ngữ lớn (LLM - Gemini) để giải đáp thắc mắc và đưa ra lời khuyên tài chính.
*   **Phân tích dữ liệu:** Biểu đồ thống kê trực quan và các báo cáo chi tiết về xu hướng tiêu dùng.

### 3. Hệ Thống & Bảo Mật
*   **Xác thực:** Đăng nhập/Đăng ký qua Email và Google (Firebase Auth).
*   **Đồng bộ dữ liệu:** Lưu trữ dữ liệu thời gian thực trên Firestore, đảm bảo đồng bộ giữa các thiết bị.
*   **Bảo mật sinh trắc học:** Hỗ trợ khóa ứng dụng bằng vân tay (BiometricPrompt).
*   **Giao diện:** Hỗ trợ Dark Mode (Chế độ tối) và Light Mode (Chế độ sáng).

## Công Nghệ Sử Dụng

| Thành Phần | Công Nghệ / Thư Viện |
|---|---|
| **Ngôn ngữ** | Java, Kotlin |
| **Kiến trúc** | MVVM (Model-View-ViewModel) |
| **Giao diện** | Material Design 3, XML Layouts |
| **Cơ sở dữ liệu** | Room Database (Local), Firestore (Cloud) |
| **Xác thực** | Firebase Authentication |
| **AI & ML** | Google Gemini API, ML Kit (Text Recognition) |
| **Biểu đồ** | MPAndroidChart |
| **Xử lý bất đồng bộ** | Coroutines, LiveData |
| **Tiện ích khác** | ViewBinding, SharedPreferences |

## Cài Đặt & Hướng Dẫn

### Yêu cầu hệ thống
*   Android Studio Hedgehog trở lên.
*   Min SDK: 24 (Android 7.0).
*   Target SDK: 34 (Android 14).
*   JDK 17.

### Các bước cài đặt
1.  Sao chép mã nguồn:
    ```bash
    git clone https://github.com/Mphuc310771/Moblie-FinalProJ.git
    ```
2.  Mở dự án trong Android Studio.
3.  Cấu hình Firebase:
    *   Tải file `google-services.json` từ Firebase Console.
    *   Đặt file vào thư mục `app/`.
4.  Đồng bộ Gradle và chạy ứng dụng.

## Tác Giả

*   **Mphuc310771** - Phát triển ứng dụng

---
© 2026 SmartBudget Project.

---

# SmartBudget (English)

A personal financial management application for Android, integrating Artificial Intelligence (AI) and Optical Character Recognition (OCR).

## Overview

SmartBudget provides a comprehensive income and expense tracking solution, helping users manage their budget effectively through an intuitive interface and automated tools.

## Key Features

### 1. Financial Management
*   **Transaction Logging:** Add, edit, and delete income/expense records by category.
*   **Budgeting:** Set spending limits for categories and receive alerts when over budget.
*   **Savings:** Track progress towards specific financial goals.

### 2. Automation & AI
*   **OCR (Optical Character Recognition):** Scan and extract information (total amount, date, content) from paper receipts using Google ML Kit Vision.
*   **AI Assistant:** Integrated Large Language Model (Gemini/Groq) to answer queries and provide financial advice (supports Multi-Model switching).
*   **Data Analysis:** Visual statistical charts and detailed reports on consumption trends.

### 3. System & Security
*   **Authentication:** Login/Register via Email and Google (Firebase Auth).
*   **Data Sync:** Real-time data storage on Firestore, ensuring synchronization across devices.
*   **Biometric Security:** App lock support via fingerprint (BiometricPrompt).
*   **UI/UX:** Supports Dark Mode and Light Mode with adaptive theming.

## Tech Stack

| Component | Technology / Library |
|---|---|
| **Language** | Java, Kotlin |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **UI** | Material Design 3, XML Layouts |
| **Database** | Room Database (Local), Firestore (Cloud) |
| **Authentication** | Firebase Authentication |
| **AI & ML** | Google Gemini API, Groq API, ML Kit (Text Recognition) |
| **Charts** | MPAndroidChart |
| **Concurrency** | Coroutines, LiveData |

## Installation & Setup

### Requirements
*   Android Studio Hedgehog or later.
*   Min SDK: 24 (Android 7.0).
*   Target SDK: 34 (Android 14).
*   JDK 17.

### Steps
1.  Clone the repository:
    ```bash
    git clone https://github.com/Mphuc310771/Moblie-FinalProJ.git
    ```
2.  Open the project in Android Studio.
3.  Configure Firebase:
    *   Download `google-services.json` from Firebase Console.
    *   Place it in the `app/` directory.
4.  Sync Gradle and run the application.

## Author

*   **Mphuc310771** - Application Developer

