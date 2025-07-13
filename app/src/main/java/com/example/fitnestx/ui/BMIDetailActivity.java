package com.example.fitnestx.ui;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnestx.R;
import com.example.fitnestx.data.entity.UserMetricsEntity;
import com.example.fitnestx.data.repository.UserMetricsRepository;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BMIDetailActivity extends AppCompatActivity {
    private TextView tvBMIValue, tvBMIStatus, tvBMIDescription, tvNutritionAdvice;
    private LineChart bmiChart;
    private ImageButton btnBack;
    private UserMetricsRepository userMetricsRepository;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_detail);

        userMetricsRepository = new UserMetricsRepository(this);
        initViews();
        loadBMIData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvBMIValue = findViewById(R.id.tv_bmi_value);
        tvBMIStatus = findViewById(R.id.tv_bmi_status);
        tvBMIDescription = findViewById(R.id.tv_bmi_description);
        tvNutritionAdvice = findViewById(R.id.tv_nutrition_advice);
        bmiChart = findViewById(R.id.bmi_chart);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadBMIData() {
        int userId = getCurrentUserId();
        if (userId == -1) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        executorService.execute(() -> {
            UserMetricsEntity userMetrics = userMetricsRepository.getUserMetricByUserId(userId);
            if (userMetrics != null) {
                runOnUiThread(() -> setupBMIDetails(userMetrics));
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Không tìm thấy thông tin BMI", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void setupBMIDetails(UserMetricsEntity userMetrics) {
        double bmi = userMetrics.getBmi();
        tvBMIValue.setText(String.format("%.1f", bmi));

        String status;
        String description;
        String nutritionAdvice;
        int statusColor;

        if (bmi < 18.5) {
            status = "Thiếu cân";
            statusColor = Color.parseColor("#2196F3");
            description = "Chỉ số BMI của bạn cho thấy bạn đang thiếu cân. Điều này có thể ảnh hưởng đến sức khỏe tổng thể và khả năng miễn dịch của cơ thể. Tình trạng thiếu cân có thể do nhiều nguyên nhân như chế độ ăn uống không đủ dinh dưỡng, stress, bệnh lý tiềm ẩn hoặc di truyền.";

            nutritionAdvice = "🍽️ CHƯƠNG TRÌNH TĂNG CÂN AN TOÀN VÀ HIỆU QUẢ\n\n" +
                    "📊 MỤC TIÊU: Tăng 0.5-1kg mỗi tháng một cách lành mạnh\n\n" +
                    "🥗 CHẾ ĐỘ DINH DƯỠNG CHI TIẾT:\n\n" +
                    "• TĂNG LƯỢNG CALO TIÊU THỤ:\n" +
                    "  - Tăng 300-500 calo/ngày so với nhu cầu cơ bản\n" +
                    "  - Ăn 5-6 bữa nhỏ thay vì 3 bữa lớn\n" +
                    "  - Không bỏ bữa, đặc biệt là bữa sáng\n\n" +
                    "• PROTEIN CHẤT LƯỢNG CAO (1.2-1.6g/kg cân nặng):\n" +
                    "  - Thịt nạc: thịt bò, thịt heo, thịt gà\n" +
                    "  - Cá và hải sản: cá hồi, cá thu, tôm, cua\n" +
                    "  - Trứng: 2-3 quả/ngày\n" +
                    "  - Sữa và sản phẩm từ sữa: sữa tươi, yaourt, phô mai\n" +
                    "  - Đậu phụ, đậu nành, các loại đậu\n\n" +
                    "• CARBOHYDRATE PHỨC HỢP:\n" +
                    "  - Gạo lứt, yến mạch, quinoa\n" +
                    "  - Khoai lang, khoai tây\n" +
                    "  - Bánh mì nguyên cám\n" +
                    "  - Mì ống nguyên cám\n\n" +
                    "• CHẤT BÉO LÀNH MẠNH:\n" +
                    "  - Bơ, dầu olive, dầu dừa\n" +
                    "  - Các loại hạt: óc chó, hạnh nhân, hạt điều\n" +
                    "  - Cá béo: cá hồi, cá thu, cá sardine\n\n" +
                    "🥤 NƯỚC UỐNG VÀ BỔ SUNG:\n" +
                    "  - Uống 2-2.5 lít nước/ngày\n" +
                    "  - Sinh tố trái cây với sữa\n" +
                    "  - Nước ép rau củ tươi\n" +
                    "  - Tránh uống nước trước bữa ăn 30 phút\n\n" +
                    "🏋️ BÀI TẬP TĂNG CÂN:\n" +
                    "  - Tập tạ 3-4 lần/tuần để tăng khối lượng cơ\n" +
                    "  - Tập compound exercises: squat, deadlift, bench press\n" +
                    "  - Hạn chế cardio quá nhiều\n" +
                    "  - Nghỉ ngơi đầy đủ giữa các buổi tập\n\n" +
                    "😴 NGHỈ NGƠI VÀ PHỤC HỒI:\n" +
                    "  - Ngủ 7-9 tiếng mỗi đêm\n" +
                    "  - Tránh stress và căng thẳng\n" +
                    "  - Thiền định hoặc yoga nhẹ\n\n" +
                    "⚠️ LƯU Ý QUAN TRỌNG:\n" +
                    "  - Tăng cân từ từ, không vội vàng\n" +
                    "  - Theo dõi cân nặng hàng tuần\n" +
                    "  - Tham khảo ý kiến bác sĩ nếu cần\n" +
                    "  - Tránh thực phẩm junk food để tăng cân\n" +
                    "  - Kiên trì ít nhất 3-6 tháng để thấy kết quả rõ rệt";

        } else if (bmi < 25) {
            status = "Bình thường";
            statusColor = Color.parseColor("#4CAF50");
            description = "Chúc mừng! Chỉ số BMI của bạn nằm trong khoảng lý tưởng. Điều này cho thấy cân nặng của bạn phù hợp với chiều cao và có nguy cơ thấp mắc các bệnh liên quan đến cân nặng. Tuy nhiên, BMI chỉ là một chỉ số tham khảo, bạn vẫn cần duy trì lối sống lành mạnh.";

            nutritionAdvice = "🎯 CHƯƠNG TRÌNH DUY TRÌ CÂN NẶNG LÝ TƯỞNG\n\n" +
                    "📊 MỤC TIÊU: Duy trì cân nặng ổn định và sức khỏe tối ưu\n\n" +
                    "🥗 CHẾ ĐỘ DINH DƯỠNG CÂN BẰNG:\n\n" +
                    "• NGUYÊN TẮC 80/20:\n" +
                    "  - 80% thực phẩm lành mạnh, dinh dưỡng\n" +
                    "  - 20% thực phẩm yêu thích (linh hoạt)\n" +
                    "  - Không cấm đoán hoàn toàn bất kỳ thực phẩm nào\n\n" +
                    "• PROTEIN ĐỦ ĐẠM (0.8-1.2g/kg cân nặng):\n" +
                    "  - Cá: 2-3 lần/tuần\n" +
                    "  - Thịt nạc: gà, bò, heo (không da, không mỡ)\n" +
                    "  - Trứng: 1-2 quả/ngày\n" +
                    "  - Đậu phụ, tempeh, các loại đậu\n" +
                    "  - Sữa chua Hy Lạp, cottage cheese\n\n" +
                    "• CARBOHYDRATE THÔNG MINH:\n" +
                    "  - Ưu tiên carb phức hợp: yến mạch, gạo lứt\n" +
                    "  - Rau củ: khoai lang, củ cải, cà rốt\n" +
                    "  - Trái cây tươi: táo, cam, berry\n" +
                    "  - Hạn chế đường tinh luyện và bánh kẹo\n\n" +
                    "• CHẤT BÉO CÂN BẰNG:\n" +
                    "  - Dầu olive, dầu bơ cho nấu ăn\n" +
                    "  - Các loại hạt: 1 nắm tay/ngày\n" +
                    "  - Bơ: 1/2 quả/ngày\n" +
                    "  - Cá béo: omega-3 tự nhiên\n\n" +
                    "🥬 RAU XANH VÀ CHẤT XƠ:\n" +
                    "  - Ít nhất 5 phần rau củ quả/ngày\n" +
                    "  - Rau lá xanh đậm: rau bina, cải kale\n" +
                    "  - Rau họ cải: bông cải xanh, súp lơ\n" +
                    "  - Chất xơ: 25-35g/ngày\n\n" +
                    "💧 HYDRATION:\n" +
                    "  - 8-10 ly nước/ngày\n" +
                    "  - Nước lọc, trà xanh, trà thảo mộc\n" +
                    "  - Hạn chế đồ uống có đường\n\n" +
                    "🏃 HOẠT ĐỘNG THỂ CHẤT:\n" +
                    "  - 150 phút cardio vừa phải/tuần\n" +
                    "  - 2-3 buổi tập tạ/tuần\n" +
                    "  - Yoga hoặc pilates cho flexibility\n" +
                    "  - Đi bộ 8000-10000 bước/ngày\n\n" +
                    "😴 QUẢN LÝ STRESS VÀ GIẤC NGỦ:\n" +
                    "  - Ngủ 7-9 tiếng chất lượng\n" +
                    "  - Thiền định 10-15 phút/ngày\n" +
                    "  - Quản lý stress hiệu quả\n" +
                    "  - Tránh ăn khuya\n\n" +
                    "📊 THEO DÕI VÀ ĐÁNH GIÁ:\n" +
                    "  - Cân nặng 1 lần/tuần, cùng thời điểm\n" +
                    "  - Đo vòng eo, vòng mông hàng tháng\n" +
                    "  - Chụp ảnh tiến trình\n" +
                    "  - Kiểm tra sức khỏe định kỳ 6 tháng/lần\n\n" +
                    "🎉 TIPS DUY TRÌ ĐỘNG LỰC:\n" +
                    "  - Đặt mục tiêu nhỏ, thực tế\n" +
                    "  - Thưởng cho bản thân khi đạt mục tiêu\n" +
                    "  - Tìm partner tập luyện\n" +
                    "  - Thay đổi menu và bài tập định kỳ";

        } else if (bmi < 30) {
            status = "Thừa cân";
            statusColor = Color.parseColor("#FF9800");
            description = "Chỉ số BMI của bạn cho thấy bạn đang thừa cân. Điều này làm tăng nguy cơ mắc các bệnh như tiểu đường type 2, bệnh tim mạch, và một số loại ung thư. Tuy nhiên, với kế hoạch giảm cân phù hợp, bạn hoàn toàn có thể cải thiện tình trạng này.";

            nutritionAdvice = "🎯 CHƯƠNG TRÌNH GIẢM CÂN KHOA HỌC VÀ BỀN VỮNG\n\n" +
                    "📊 MỤC TIÊU: Giảm 0.5-1kg/tuần một cách an toàn\n\n" +
                    "🔥 NGUYÊN TẮC THÂM HỤT CALO:\n" +
                    "  - Tạo thâm hụt 300-500 calo/ngày\n" +
                    "  - Kết hợp giảm calo ăn vào + tăng calo đốt cháy\n" +
                    "  - Không giảm quá 1000 calo/ngày\n\n" +
                    "🥗 CHẾ ĐỘ ĂN GIẢM CÂN HIỆU QUẢ:\n\n" +
                    "• PROTEIN CAO (1.2-1.6g/kg cân nặng):\n" +
                    "  - Ưu tiên protein nạc: ức gà, cá, tôm\n" +
                    "  - Trứng trắng, whey protein\n" +
                    "  - Đậu phụ, tempeh cho người ăn chay\n" +
                    "  - Sữa chua Hy Lạp không đường\n" +
                    "  - Protein giúp no lâu và giữ khối lượng cơ\n\n" +
                    "• CARBOHYDRATE THÔNG MINH:\n" +
                    "  - Giảm 30-40% lượng carb hiện tại\n" +
                    "  - Chọn carb phức hợp: yến mạch, gạo lứt\n" +
                    "  - Rau củ thay thế: khoai lang, củ cải\n" +
                    "  - Tránh hoàn toàn: bánh kẹo, nước ngọt\n" +
                    "  - Ăn carb chủ yếu vào buổi sáng và trước tập\n\n" +
                    "• CHẤT BÉO LÀNH MẠNH (20-25% tổng calo):\n" +
                    "  - Dầu olive, dầu bơ (1-2 thìa/ngày)\n" +
                    "  - Các loại hạt: 1 nắm nhỏ/ngày\n" +
                    "  - Bơ: 1/4 quả/ngày\n" +
                    "  - Tránh chất béo trans và bão hòa\n\n" +
                    "🥬 RAU XANH VÀ CHẤT XƠ:\n" +
                    "  - Rau xanh chiếm 50% đĩa ăn\n" +
                    "  - Ưu tiên rau ít calo: rau muống, cải bó xôi\n" +
                    "  - Chất xơ 30-40g/ngày để no lâu\n" +
                    "  - Ăn rau trước protein và carb\n\n" +
                    "⏰ THỜI GIAN ĂN UỐNG:\n" +
                    "  - Intermittent Fasting 16:8 (nếu phù hợp)\n" +
                    "  - Ăn sáng đầy đủ, tối nhẹ nhàng\n" +
                    "  - Ngừng ăn 3 tiếng trước khi ngủ\n" +
                    "  - Ăn chậm, nhai kỹ 20-30 lần/miếng\n\n" +
                    "💧 HYDRATION VÀ DETOX:\n" +
                    "  - 3-4 lít nước/ngày\n" +
                    "  - Uống 500ml nước trước mỗi bữa ăn\n" +
                    "  - Trà xanh, trà oolong hỗ trợ đốt cháy\n" +
                    "  - Nước chanh mật ong buổi sáng\n\n" +
                    "🏃 CHƯƠNG TRÌNH TẬP LUYỆN:\n\n" +
                    "• CARDIO (5-6 lần/tuần):\n" +
                    "  - HIIT: 20-30 phút, 3 lần/tuần\n" +
                    "  - LISS: 45-60 phút, 2-3 lần/tuần\n" +
                    "  - Đi bộ nhanh: 10000+ bước/ngày\n\n" +
                    "• TẬP TẠ (3-4 lần/tuần):\n" +
                    "  - Compound exercises: squat, deadlift\n" +
                    "  - Circuit training để đốt cháy tối đa\n" +
                    "  - Rep cao (12-15), nghỉ ngắn (30-45s)\n\n" +
                    "😴 NGHỈ NGƠI VÀ PHỤC HỒI:\n" +
                    "  - Ngủ 7-8 tiếng để điều hòa hormone\n" +
                    "  - Quản lý stress: cortisol cao = tích mỡ\n" +
                    "  - Massage, sauna 1-2 lần/tuần\n\n" +
                    "📊 THEO DÕI TIẾN TRÌNH:\n" +
                    "  - Cân nặng: 2 lần/tuần, cùng điều kiện\n" +
                    "  - Đo vòng eo, mông, đùi hàng tuần\n" +
                    "  - Chụp ảnh before/after\n" +
                    "  - Ghi nhật ký ăn uống và tập luyện\n\n" +
                    "⚠️ TRÁNH CÁC SAI LẦM:\n" +
                    "  - Không nhịn ăn quá mức\n" +
                    "  - Không cắt carb hoàn toàn\n" +
                    "  - Không chỉ tập cardio mà bỏ tạ\n" +
                    "  - Không mong đợi kết quả nhanh chóng\n" +
                    "  - Kiên trì ít nhất 3-6 tháng";

        } else {
            status = "Béo phì";
            statusColor = Color.parseColor("#F44336");
            description = "Chỉ số BMI của bạn cho thấy tình trạng béo phì, điều này làm tăng đáng kể nguy cơ mắc các bệnh nghiêm trọng như bệnh tim, đột quỵ, tiểu đường, và một số loại ung thư. Bạn cần có kế hoạch can thiệp nghiêm túc và có thể cần sự hỗ trợ của chuyên gia y tế.";

            nutritionAdvice = "🚨 CHƯƠNG TRÌNH CAN THIỆP GIẢM CÂN NGHIÊM TÚC\n\n" +
                    "⚠️ QUAN TRỌNG: Tham khảo bác sĩ trước khi bắt đầu!\n\n" +
                    "📊 MỤC TIÊU GIAI ĐOẠN:\n" +
                    "  - Giai đoạn 1: Giảm 5-10% cân nặng trong 6 tháng\n" +
                    "  - Giai đoạn 2: Tiếp tục giảm đến BMI < 30\n" +
                    "  - Mục tiêu cuối: Đạt BMI 18.5-24.9\n\n" +
                    "🔥 THÂM HỤT CALO TÍCH CỰC:\n" +
                    "  - Thâm hụt 500-750 calo/ngày\n" +
                    "  - Có thể lên đến 1000 calo/ngày (dưới giám sát)\n" +
                    "  - Theo dõi chặt chẽ để tránh suy dinh dưỡng\n\n" +
                    "🥗 CHẾ ĐỘ ĂN CẤP THIẾT:\n\n" +
                    "• PROTEIN RẤT CAO (1.6-2.0g/kg cân nặng):\n" +
                    "  - Ức gà, cá trắng, tôm cua\n" +
                    "  - Trứng trắng (6-8 quả/ngày)\n" +
                    "  - Whey protein isolate\n" +
                    "  - Đậu phụ, seitan cho người chay\n" +
                    "  - Protein giúp no lâu và bảo vệ cơ bắp\n\n" +
                    "• CARBOHYDRATE KIỂM SOÁT NGHIÊM:\n" +
                    "  - Giảm 50-60% lượng carb hiện tại\n" +
                    "  - Chỉ ăn carb phức hợp: yến mạch, quinoa\n" +
                    "  - Rau củ thay thế hoàn toàn tinh bột\n" +
                    "  - Loại bỏ hoàn toàn: đường, bánh, nước ngọt\n" +
                    "  - Carb chỉ ăn sau tập luyện\n\n" +
                    "• CHẤT BÉO TỐI THIỂU (15-20% tổng calo):\n" +
                    "  - Dầu olive: 1 thìa/ngày\n" +
                    "  - Cá béo: 2 lần/tuần cho omega-3\n" +
                    "  - Tránh hoàn toàn: đồ chiên, fast food\n\n" +
                    "🥬 RAU XANH LÀ CHỦ ĐẠO:\n" +
                    "  - 70% đĩa ăn là rau xanh\n" +
                    "  - Rau ít calo: rau muống, cải bó xôi, súp lơ\n" +
                    "  - Chất xơ 40-50g/ngày\n" +
                    "  - Ăn rau trước mọi thứ khác\n\n" +
                    "⏰ LỊCH ĂN UỐNG NGHIÊM NGẶT:\n" +
                    "  - Intermittent Fasting 18:6 hoặc 20:4\n" +
                    "  - 2-3 bữa chính, không ăn vặt\n" +
                    "  - Ngừng ăn 4 tiếng trước ngủ\n" +
                    "  - Meal prep để kiểm soát portion\n\n" +
                    "💧 HYDRATION TÍCH CỰC:\n" +
                    "  - 4-5 lít nước/ngày\n" +
                    "  - 1 lít nước trước mỗi bữa ăn\n" +
                    "  - Trà xanh, cà phê đen không đường\n" +
                    "  - Nước chanh muối buổi sáng\n\n" +
                    "🏃 CHƯƠNG TRÌNH TẬP LUYỆN TÍCH CỰC:\n\n" +
                    "• CARDIO HÀNG NGÀY:\n" +
                    "  - Bắt đầu: 30 phút đi bộ nhanh\n" +
                    "  - Tuần 2-4: 45 phút cardio vừa phải\n" +
                    "  - Tuần 5+: HIIT 30 phút + LISS 30 phút\n" +
                    "  - Mục tiêu: 15000+ bước/ngày\n\n" +
                    "• TẬP TẠ (4-5 lần/tuần):\n" +
                    "  - Full body workout\n" +
                    "  - Circuit training cường độ cao\n" +
                    "  - Rep cao (15-20), nghỉ ngắn (30s)\n" +
                    "  - Tập tạ giúp duy trì khối lượng cơ\n\n" +
                    "• HOẠT ĐỘNG KHÁC:\n" +
                    "  - Bơi lội: ít tác động lên khớp\n" +
                    "  - Yoga: cải thiện flexibility\n" +
                    "  - Đi cầu thang thay thang máy\n\n" +
                    "😴 QUẢN LÝ STRESS VÀ GIẤC NGỦ:\n" +
                    "  - Ngủ 7-8 tiếng chất lượng\n" +
                    "  - Thiền định 20-30 phút/ngày\n" +
                    "  - Tránh stress eating\n" +
                    "  - Tìm support group hoặc coach\n\n" +
                    "📊 THEO DÕI CHẶT CHẼ:\n" +
                    "  - Cân nặng: hàng ngày, cùng thời điểm\n" +
                    "  - Đo vòng eo, mông 2 lần/tuần\n" +
                    "  - Chụp ảnh tiến trình hàng tuần\n" +
                    "  - Ghi nhật ký chi tiết mọi thứ ăn uống\n" +
                    "  - Kiểm tra sức khỏe hàng tháng\n\n" +
                    "🏥 HỖ TRỢ Y TẾ:\n" +
                    "  - Tham khảo bác sĩ dinh dưỡng\n" +
                    "  - Kiểm tra hormone, tuyến giáp\n" +
                    "  - Theo dõi huyết áp, đường huyết\n" +
                    "  - Cân nhắc thuốc hỗ trợ nếu cần\n\n" +
                    "💪 ĐỘNG LỰC VÀ KIÊN TRÌ:\n" +
                    "  - Đặt mục tiêu nhỏ hàng tuần\n" +
                    "  - Tìm accountability partner\n" +
                    "  - Thưởng bản thân (không phải đồ ăn)\n" +
                    "  - Tham gia group hỗ trợ\n" +
                    "  - Nhớ rằng đây là cuộc marathon, không phải sprint\n\n" +
                    "🚫 TUYỆT ĐỐI TRÁNH:\n" +
                    "  - Crash diet hoặc fad diet\n" +
                    "  - Thuốc giảm cân không rõ nguồn gốc\n" +
                    "  - Tự ti, stress quá mức\n" +
                    "  - Bỏ cuộc khi gặp plateau\n\n" +
                    "⏳ TIMELINE THỰC TẾ:\n" +
                    "  - Tháng 1-2: Thích nghi với chế độ mới\n" +
                    "  - Tháng 3-6: Giảm cân ổn định\n" +
                    "  - Tháng 6-12: Đạt mục tiêu trung hạn\n" +
                    "  - Năm 2+: Duy trì cân nặng lý tưởng\n\n" +
                    "Hãy nhớ: Béo phì là bệnh lý cần điều trị nghiêm túc. Sự kiên trì và hỗ trợ chuyên nghiệp là chìa khóa thành công!";
        }

        tvBMIStatus.setText(status);
        tvBMIStatus.setTextColor(statusColor);
        tvBMIDescription.setText(description);
        tvNutritionAdvice.setText(nutritionAdvice);

        setupBMIChart(bmi, statusColor);
    }

    private void setupBMIChart(double currentBMI, int color) {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0f, (float) currentBMI));

        LineDataSet dataSet = new LineDataSet(entries, "BMI hiện tại");
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setLineWidth(4f);
        dataSet.setCircleRadius(8f);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(color);

        LineData lineData = new LineData(dataSet);
        bmiChart.setData(lineData);

        // Tùy chỉnh biểu đồ
        Description description = new Description();
        description.setText("");
        bmiChart.setDescription(description);

        XAxis xAxis = bmiChart.getXAxis();
        xAxis.setEnabled(false);

        YAxis leftAxis = bmiChart.getAxisLeft();
        leftAxis.setAxisMinimum(15f);
        leftAxis.setAxisMaximum(40f);
        leftAxis.setTextColor(Color.GRAY);
        leftAxis.setTextSize(12f);

        YAxis rightAxis = bmiChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Thêm các vùng BMI
        leftAxis.removeAllLimitLines();

        com.github.mikephil.charting.components.LimitLine underweight =
                new com.github.mikephil.charting.components.LimitLine(18.5f, "Thiếu cân");
        underweight.setLineColor(Color.parseColor("#2196F3"));
        underweight.setLineWidth(2f);
        underweight.setTextColor(Color.parseColor("#2196F3"));
        leftAxis.addLimitLine(underweight);

        com.github.mikephil.charting.components.LimitLine normal =
                new com.github.mikephil.charting.components.LimitLine(25f, "Bình thường");
        normal.setLineColor(Color.parseColor("#4CAF50"));
        normal.setLineWidth(2f);
        normal.setTextColor(Color.parseColor("#4CAF50"));
        leftAxis.addLimitLine(normal);

        com.github.mikephil.charting.components.LimitLine overweight =
                new com.github.mikephil.charting.components.LimitLine(30f, "Béo phì");
        overweight.setLineColor(Color.parseColor("#F44336"));
        overweight.setLineWidth(2f);
        overweight.setTextColor(Color.parseColor("#F44336"));
        leftAxis.addLimitLine(overweight);

        bmiChart.getLegend().setEnabled(false);
        bmiChart.setTouchEnabled(false);
        bmiChart.invalidate();
    }

    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        return prefs.getInt("userId", -1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
