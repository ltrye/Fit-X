package com.example.fitnestx.ui;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvAge, tvGender, tvEmail, tvWeight, tvHeight, tvBMI, tvGoal;
    private LineChart bmiChart;
    private ImageButton btnBack;
    private Button btnEditUserInfo, btnEditMetrics;

    private UserRepository userRepository;
    private UserMetricsRepository userMetricsRepository;
    private ExecutorService executorService;

    // Store current user data for editing
    private UserEntity currentUser;
    private UserMetricsEntity currentMetrics;

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
        btnEditUserInfo = findViewById(R.id.btn_edit_user_info);
        btnEditMetrics = findViewById(R.id.btn_edit_metrics);

        // Set click listeners
        btnBack.setOnClickListener(v -> finish());
        btnEditUserInfo.setOnClickListener(v -> showEditUserInfoDialog());
        btnEditMetrics.setOnClickListener(v -> showEditMetricsDialog());
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
                    currentUser = user; // Store reference for editing
                    displayUserInfo(user);
                }
                if (userMetrics != null) {
                    currentMetrics = userMetrics; // Store reference for editing
                    displayUserMetrics(userMetrics);
                    setupBMIChart(userId); // Pass userId to load all metrics
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

    /**
     * Setup BMI chart with historical data from database
     */
    private void setupBMIChart(int userId) {
        executorService.execute(() -> {
            try {
                // Get all user metrics from database
                List<UserMetricsEntity> allMetrics = userMetricsRepository.getAllUserMetricsByUserId(userId);

                runOnUiThread(() -> {
                    displayBMIChart(allMetrics);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    // If error occurs, show single point chart with current BMI
                    List<UserMetricsEntity> singleMetric = new ArrayList<>();
                    if (currentMetrics != null) {
                        singleMetric.add(currentMetrics);
                    }
                    displayBMIChart(singleMetric);
                });
            }
        });
    }

    /**
     * Display BMI chart with given metrics data
     */
    private void displayBMIChart(List<UserMetricsEntity> metrics) {
        List<Entry> entries = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        if (metrics == null || metrics.isEmpty()) {
            // Show empty chart message
            bmiChart.clear();
            bmiChart.setNoDataText("Không có dữ liệu BMI");
            return;
        }

        // Sort metrics by timestamp to ensure chronological order
        Collections.sort(metrics, (m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));

        // Create chart entries from metrics data
        for (int i = 0; i < metrics.size(); i++) {
            UserMetricsEntity metric = metrics.get(i);
            // Use index as x-value for simplicity, or convert timestamp to days since first
            // entry
            float xValue = i; // Simple approach: use entry index
            float yValue = (float) metric.getBmi();
            entries.add(new Entry(xValue, yValue));
        }

        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "BMI theo thời gian");
        dataSet.setColor(Color.parseColor("#4CAF50")); // Green color
        dataSet.setCircleColor(Color.parseColor("#4CAF50"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(true);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Smooth curve

        LineData lineData = new LineData(dataSet);
        bmiChart.setData(lineData);

        // Customize chart appearance
        Description description = new Description();
        description.setText("Biểu đồ BMI theo thời gian");
        description.setTextSize(12f);
        bmiChart.setDescription(description);

        // Configure X-axis
        XAxis xAxis = bmiChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(true);

        // Configure Y-axis
        YAxis leftAxis = bmiChart.getAxisLeft();
        leftAxis.setAxisMinimum(5f);
        leftAxis.setAxisMaximum(70f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = bmiChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Add BMI category limit lines
        com.github.mikephil.charting.components.LimitLine underweightLine = new com.github.mikephil.charting.components.LimitLine(
                18.5f, "Thiếu cân");
        underweightLine.setLineColor(Color.BLUE);
        underweightLine.setLineWidth(1f);

        com.github.mikephil.charting.components.LimitLine normalLine = new com.github.mikephil.charting.components.LimitLine(
                25f, "Bình thường");
        normalLine.setLineColor(Color.GREEN);
        normalLine.setLineWidth(1f);

        com.github.mikephil.charting.components.LimitLine overweightLine = new com.github.mikephil.charting.components.LimitLine(
                30f, "Thừa cân");
        overweightLine.setLineColor(Color.YELLOW);
        overweightLine.setLineWidth(1f);

        leftAxis.addLimitLine(underweightLine);
        leftAxis.addLimitLine(normalLine);
        leftAxis.addLimitLine(overweightLine);

        // Enable chart interactions
        bmiChart.setTouchEnabled(true);
        bmiChart.setDragEnabled(true);
        bmiChart.setScaleEnabled(true);
        bmiChart.setPinchZoom(true);

        // Animate chart
        bmiChart.animateX(1000);
        bmiChart.invalidate();
    }

    /**
     * Show dialog to edit user basic information (email, age, gender)
     */
    private void showEditUserInfoDialog() {
        if (currentUser == null) {
            Toast.makeText(this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(android.R.layout.simple_list_item_1, null);

        // Create custom dialog content
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 50, 50, 50);


        // Name Input
        TextView nameLabel = new TextView(this);
        nameLabel.setText("Name:");
        nameLabel.setTextSize(16);
        nameLabel.setPadding(0, 20, 0, 8);

        EditText etName = new EditText(this);
        etName.setText(currentUser.getName());
        etName.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        // Email input
        TextView emailLabel = new TextView(this);
        emailLabel.setText("Email:");
        emailLabel.setTextSize(16);
        emailLabel.setPadding(0, 20, 0, 8);

        EditText etEmail = new EditText(this);
        etEmail.setText(currentUser.getEmail());
        etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        // Age input
        TextView ageLabel = new TextView(this);
        ageLabel.setText("Tuổi:");
        ageLabel.setTextSize(16);
        ageLabel.setPadding(0, 20, 0, 8);

        EditText etAge = new EditText(this);
        etAge.setText(String.valueOf(currentUser.getAge()));
        etAge.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        // Gender selection
        TextView genderLabel = new TextView(this);
        genderLabel.setText("Giới tính:");
        genderLabel.setTextSize(16);
        genderLabel.setPadding(0, 20, 0, 8);

        RadioGroup rgGender = new RadioGroup(this);
        RadioButton rbMale = new RadioButton(this);
        rbMale.setText("Nam");
        rbMale.setId(View.generateViewId());
        RadioButton rbFemale = new RadioButton(this);
        rbFemale.setText("Nữ");
        rbFemale.setId(View.generateViewId());

        rgGender.addView(rbMale);
        rgGender.addView(rbFemale);

        // Set current gender
        if (currentUser.getGender()) {
            rbMale.setChecked(true);
        } else {
            rbFemale.setChecked(true);
        }

        // Add views to layout
        dialogLayout.addView(nameLabel);
        dialogLayout.addView(etName);
        dialogLayout.addView(emailLabel);
        dialogLayout.addView(etEmail);
        dialogLayout.addView(ageLabel);
        dialogLayout.addView(etAge);
        dialogLayout.addView(genderLabel);
        dialogLayout.addView(rgGender);

        // Create and show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa thông tin cá nhân");
        builder.setView(dialogLayout);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            try {
                String newEmail = etEmail.getText().toString().trim();
                int newAge = Integer.parseInt(etAge.getText().toString().trim());
                boolean newGender = rgGender.getCheckedRadioButtonId() == rbMale.getId();
                String newName = etName.getText().toString().trim();
                // Validate input
                if (newEmail.isEmpty() || newAge < 1 || newAge > 150) {
                    Toast.makeText(this, "Vui lòng nhập thông tin hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateUserInfo(newName,newEmail, newAge, newGender);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Vui lòng nhập tuổi hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    /**
     * Show dialog to edit user metrics (weight, height)
     */
    private void showEditMetricsDialog() {
        if (currentMetrics == null) {
            Toast.makeText(this, "Không thể tải thông tin chỉ số", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create dialog layout
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 50, 50, 50);

        // Weight input
        TextView weightLabel = new TextView(this);
        weightLabel.setText("Cân nặng (kg):");
        weightLabel.setTextSize(16);
        weightLabel.setPadding(0, 20, 0, 8);

        EditText etWeight = new EditText(this);
        etWeight.setText(String.valueOf(currentMetrics.getWeight()));
        etWeight.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // Height input
        TextView heightLabel = new TextView(this);
        heightLabel.setText("Chiều cao (cm):");
        heightLabel.setTextSize(16);
        heightLabel.setPadding(0, 20, 0, 8);

        EditText etHeight = new EditText(this);
        etHeight.setText(String.valueOf(currentMetrics.getHeight()));
        etHeight.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // Goal selection
        TextView goalLabel = new TextView(this);
        goalLabel.setText("Thay đổi mục tiêu tập luyện:");
        goalLabel.setTextSize(16);
        goalLabel.setPadding(0, 20, 0, 8);

        Spinner spinnerGoal = new Spinner(this);

        // Create goal options with display names
        String[] goalValues = { "lose_fat", "improve_shape", "lean_tone" };
        String[] goalDisplayNames = { "Giảm mỡ", "Cải thiện vóc dáng", "Săn chắc cơ thể" };

        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, goalDisplayNames);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(goalAdapter);

        // Set current goal as default selection
        String currentGoal = currentMetrics.getGoal();
        int defaultSelection = 0; // Default to first option
        for (int i = 0; i < goalValues.length; i++) {
            if (goalValues[i].equals(currentGoal)) {
                defaultSelection = i;
                break;
            }
        }
        spinnerGoal.setSelection(defaultSelection);

        // Notice text about goal change impact
        TextView noticeText = new TextView(this);
        noticeText.setText("Lưu ý: Các bài tập sẽ thay đổi trong kế hoạch tập luyện tiếp theo");
        noticeText.setTextSize(12);
        noticeText.setTextColor(Color.parseColor("#FF6B35")); // Orange color for notice
        noticeText.setPadding(0, 8, 0, 0);
        noticeText.setTypeface(Typeface.defaultFromStyle(android.graphics.Typeface.ITALIC));

        // Add views to layout
        dialogLayout.addView(weightLabel);
        dialogLayout.addView(etWeight);
        dialogLayout.addView(heightLabel);
        dialogLayout.addView(etHeight);
        dialogLayout.addView(goalLabel);
        dialogLayout.addView(spinnerGoal);
        dialogLayout.addView(noticeText);

        // Create and show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập nhật chỉ số sức khỏe");
        builder.setView(dialogLayout);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            try {
                double newWeight = Double.parseDouble(etWeight.getText().toString().trim());
                double newHeight = Double.parseDouble(etHeight.getText().toString().trim());

                // Get selected goal value
                int selectedGoalIndex = spinnerGoal.getSelectedItemPosition();
                String newGoal = goalValues[selectedGoalIndex];

                // Validate input
                if (newWeight < 20 || newWeight > 300 || newHeight < 100 || newHeight > 250) {
                    Toast.makeText(this, "Vui lòng nhập thông tin hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                createNewUserMetrics(newWeight, newHeight, newGoal);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    /**
     * Update user basic information in database
     */
    private void updateUserInfo(String newName, String newEmail, int newAge, boolean newGender) {
        executorService.execute(() -> {
            try {

                UserEntity cur = userRepository.getUserById(getCurrentUserId());
            cur.setName(newName);
                cur.setEmail(newEmail);
                cur.setAge(newAge);
                cur.setGender(newGender);

                // Update in database
                try{
                userRepository.updateUserWithMailCheck(cur, getCurrentUserId());
                runOnUiThread(() -> {
                    // Update UI
                    displayUserInfo(currentUser);
                    Toast.makeText(this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                });
                // Update current user object
                currentUser.setEmail(newEmail);
                currentUser.setAge(newAge);
                currentUser.setGender(newGender);
                currentUser.setName(newName);

                }catch(RuntimeException e){
                    runOnUiThread(() -> {
                        // Update UI
                        displayUserInfo(currentUser);
                        Toast.makeText(this, "Cập nhật thông tin thất bại:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }


            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi cập nhật thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Create new user metrics entry with current timestamp
     */
    private void createNewUserMetrics(double newWeight, double newHeight, String newGoal) {
        executorService.execute(() -> {
            try {
                // Calculate BMI
                double newBMI = calculateBMI(newWeight, newHeight);

                // Get current timestamp
                String currentTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(new Date());

                // Create new metrics entity
                UserMetricsEntity newMetrics = new UserMetricsEntity(
                        0, // Auto-generated ID
                        currentUser.getUserId(),
                        currentTimestamp,
                        newWeight,
                        newHeight,
                        newBMI,
                        newGoal // Use selected goal
                );

                // Insert new metrics
                userMetricsRepository.insertUserMetric(newMetrics);

                // Update current metrics reference
                currentMetrics = newMetrics;

                runOnUiThread(() -> {
                    // Update UI
                    displayUserMetrics(newMetrics);
                    setupBMIChart(currentUser.getUserId()); // Refresh chart with all data
                    Toast.makeText(this, "Cập nhật chỉ số thành công", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi cập nhật chỉ số: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Calculate BMI from weight and height
     */
    private double calculateBMI(double weight, double height) {
        // Convert height from cm to meters
        double heightInMeters = height / 100.0;
        return weight / (heightInMeters * heightInMeters);
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
