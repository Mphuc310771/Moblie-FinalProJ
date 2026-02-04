package com.smartbudget.app.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Lifestyle Advisor.
 * Provides holistic lifestyle improvement suggestions based on spending.
 */
public class LifestyleAdvisor {

    public static class Advice {
        public String category;
        public String title;
        public String detail;
        public String imageUrl;

        public Advice(String category, String title, String detail) {
            this.category = category;
            this.title = title;
            this.detail = detail;
        }
    }

    /**
     * Generate lifestyle advice.
     */
    public static List<Advice> generateAdvice(double healthSpending, double foodSpending, double educationSpending) {
        List<Advice> adviceList = new ArrayList<>();

        // Health Analysis
        if (healthSpending < 500000) {
            adviceList.add(new Advice(
                "Sức khỏe",
                "Đầu tư thêm cho sức khỏe",
                "Bạn chi tiêu hơi ít cho sức khỏe tháng này. Hãy cân nhắc đăng ký gym hoặc mua thực phẩm bổ sung."
            ));
        } else {
            adviceList.add(new Advice(
                "Sức khỏe",
                "Duy trì lối sống lành mạnh",
                "Tuyệt vời! Bạn đang đầu tư tốt cho bản thân."
            ));
        }

        // Food Analysis
        if (foodSpending > 5000000) {
            adviceList.add(new Advice(
                "Dinh dưỡng",
                "Nấu ăn tại nhà",
                "Chi phí ăn uống đang cao. Thử nấu ăn tại nhà 3 bữa/tuần để tiết kiệm và khỏe mạnh hơn."
            ));
        }

        // Education/Self-improvement
        if (educationSpending < 200000) {
            adviceList.add(new Advice(
                "Phát triển",
                "Học kỹ năng mới",
                "Đầu tư vào tri thức mang lại lợi nhuận cao nhất. Hãy mua một cuốn sách hoặc khóa học mới."
            ));
        }

        // Work-Life Balance
        adviceList.add(new Advice(
            "Cân bằng",
            "Dành thời gian cho bản thân",
            "Đừng quên dành 10% thu nhập cho niềm vui cá nhân để tránh burnout."
        ));

        return adviceList;
    }
}
