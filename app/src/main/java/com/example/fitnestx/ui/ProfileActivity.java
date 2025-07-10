package com.example.fitnestx.ui;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnestx.R;
import com.example.fitnestx.data.entity.UserEntity;
import com.example.fitnestx.data.entity.UserMetricsEntity;
import com.example.fitnestx.data.repository.UserMetricsRepository;
import com.example.fitnestx.data.repository.UserRepository;
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

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvAge, tvGender, tvEmail, tvWeight, tvHeight, tvBMI, tvGoal;
    private LineChart bmiChart;
    private ImageButton btnBack;

    private UserRepository userRepository;
    private UserMetricsRepository userMetricsRepository;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupRepositories();
        loadUserProfile();
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_name);
        tvAge = findViewById(R.id.tv_age);
        tvGender = findViewById(R.id.tv_gender);
        tvEmail = findViewById(R.id.tv_email);
        tvWeight = findViewById(R.id.tv_weight);
        tvHeight = findViewById(R.id.tv_height);
        tvBMI = findViewById(R.id.tv_bmi);
        tvGoal = findViewById(R.id.tv_goal);
        bmiChart = findViewById(R.id.bmi_chart);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRepositories() {
        userRepository = new UserRepository(this);
        userMetricsRepository = new UserMetricsRepository(this);
        executorService = Executors.newSingleThreadExecutor();
    }

    private void loadUserProfile() {
        executorService.execute(() -> {
            int userId = getCurrentUserId();
            if (userId == -1) {
                runOnUiThread(() -> finish());
                return;
            }

            UserEntity user = userRepository.getUserById(userId);
            UserMetricsEntity userMetrics = userMetricsRepository.getUserMetricByUserId(userId);

            runOnUiThread(() -> {
                if (user != null) {
                    displayUserInfo(user);
                }
                if (userMetrics != null) {
                    displayUserMetrics(userMetrics);
                    setupBMIChart(userMetrics.getBmi());
                }
            });
        });
    }

    private void displayUserInfo(UserEntity user) {
        tvName.setText(user.getName());
        tvAge.setText(String.valueOf(user.getAge()) + " tuổi");
        tvGender.setText(user.getGender() ? "Nam" : "Nữ");
        tvEmail.setText(user.getEmail());
    }

    private void displayUserMetrics(UserMetricsEntity metrics) {
        tvWeight.setText(String.format("%.1f kg", metrics.getWeight()));
        tvHeight.setText(String.format("%.1f cm", metrics.getHeight()));
        tvBMI.setText(String.format("%.1f", metrics.getBmi()));
        tvGoal.setText(metrics.getGoal());

        // Đổi màu BMI theo mức độ
        setBMIColor(metrics.getBmi());
    }

    private void setBMIColor(double bmi) {
        int color;
        if (bmi < 18.5) {
            color = Color.BLUE; // Thiếu cân
        } else if (bmi < 25) {
            color = Color.GREEN; // Bình thường
        } else if (bmi < 30) {
            color = Color.YELLOW; // Thừa cân
        } else {
            color = Color.RED; // Béo phì
        }
        tvBMI.setTextColor(color);
    }

    private void setupBMIChart(double currentBMI) {
        List<Entry> entries = new ArrayList<>();

        // Tạo dữ liệu mẫu cho biểu đồ BMI (có thể mở rộng để lấy từ database)
        entries.add(new Entry(0f, (float) currentBMI));

        LineDataSet dataSet = new LineDataSet(entries, "BMI");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);
        bmiChart.setData(lineData);

        // Tùy chỉnh biểu đồ
        Description description = new Description();
        description.setText("Chỉ số BMI");
        bmiChart.setDescription(description);

        XAxis xAxis = bmiChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = bmiChart.getAxisLeft();
        leftAxis.setAxisMinimum(15f);
        leftAxis.setAxisMaximum(35f);

        YAxis rightAxis = bmiChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Thêm các vùng BMI
        leftAxis.addLimitLine(new com.github.mikephil.charting.components.LimitLine(18.5f, "Thiếu cân"));
        leftAxis.addLimitLine(new com.github.mikephil.charting.components.LimitLine(25f, "Bình thường"));
        leftAxis.addLimitLine(new com.github.mikephil.charting.components.LimitLine(30f, "Thừa cân"));

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
