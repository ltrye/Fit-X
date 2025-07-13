package com.example.fitnestx.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.fitnestx.R;
import com.example.fitnestx.data.entity.UserMetricsEntity;
import com.example.fitnestx.data.repository.UserMetricsRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BmiResultActivity extends AppCompatActivity {

    private TextView tvBmiValue, tvBmiStatus, tvBmiRecommendation;
    private Button btnNext;
    private ProgressBar bmiProgressBar;
    private FrameLayout bmiIndicatorView;

    private UserMetricsRepository userMetricsRepository;
    private ExecutorService executorService;

    private int planId;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_result);

        // Initialize repositories and executor
        userMetricsRepository = new UserMetricsRepository(this);
        executorService = Executors.newSingleThreadExecutor();

        // Get data from intent
        planId = getIntent().getIntExtra("planId", -1);
        userId = getIntent().getIntExtra("userId", -1);

        if (planId == -1 || userId == -1) {
            Toast.makeText(this, getString(R.string.error_missing_data), Toast.LENGTH_SHORT).show();
            Log.e("BmiResultActivity", "Missing planId or userId in intent");
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        loadBmiData();
    }

    private void initViews() {
        tvBmiValue = findViewById(R.id.tv_bmi_value);
        tvBmiStatus = findViewById(R.id.tv_bmi_status);
        tvBmiRecommendation = findViewById(R.id.tv_bmi_recommendation);
        btnNext = findViewById(R.id.btn_next);
        bmiProgressBar = findViewById(R.id.bmi_progress_bar);
        bmiIndicatorView = findViewById(R.id.bmi_indicator_view);
    }

    private void setupClickListeners() {
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(BmiResultActivity.this, PlanActivity.class);
            intent.putExtra("planId", planId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadBmiData() {
        executorService.execute(() -> {
            UserMetricsEntity userMetrics = userMetricsRepository.getUserMetricByUserId(userId);
            if (userMetrics != null) {
                runOnUiThread(() -> displayBmiResult(userMetrics));
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.error_could_not_load_bmi), Toast.LENGTH_SHORT).show();
                    Log.e("BmiResultActivity", "No UserMetricsEntity found for userId: " + userId);
                });
            }
        });
    }

    private void displayBmiResult(UserMetricsEntity userMetrics) {
        double bmi = userMetrics.getBmi();
        tvBmiValue.setText(String.format("%.1f", bmi));

        String status;
        String recommendation;
        int statusColor;
        int progressColor;

        if (bmi < 18.5) {
            status = getString(R.string.status_underweight);
            statusColor = ContextCompat.getColor(this, R.color.underweight);
            progressColor = statusColor;
            recommendation = generateRecommendation(userMetrics, getString(R.string.action_gain));
        } else if (bmi < 25) {
            status = getString(R.string.status_normal_weight);
            statusColor = ContextCompat.getColor(this, R.color.normal_weight);
            progressColor = statusColor;
            recommendation = getString(R.string.recommendation_normal);
        } else if (bmi < 30) {
            status = getString(R.string.status_overweight);
            statusColor = ContextCompat.getColor(this, R.color.overweight);
            progressColor = statusColor;
            recommendation = generateRecommendation(userMetrics, getString(R.string.action_lose));
        } else {
            status = getString(R.string.status_obese);
            statusColor = ContextCompat.getColor(this, R.color.obese);
            progressColor = statusColor;
            recommendation = generateRecommendation(userMetrics, getString(R.string.action_lose));
        }

        tvBmiStatus.setText(status);
        tvBmiStatus.setTextColor(statusColor);

        SpannableString spannableRecommendation = new SpannableString(recommendation);
        if (recommendation.contains("kg")) {
            int startIndex = recommendation.indexOf(String.format("%.1f", calculateWeightDifference(userMetrics)));
            int endIndex = recommendation.indexOf("kg") + 2;
            if (startIndex != -1){
                spannableRecommendation.setSpan(new ForegroundColorSpan(Color.RED), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        tvBmiRecommendation.setText(spannableRecommendation);

        // Update progress bar
        updateBmiProgressBar(bmi, progressColor);
        
        // Animate the BMI view
        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        bmiIndicatorView.startAnimation(scaleAnimation);
    }

    private String generateRecommendation(UserMetricsEntity metrics, String action) {
        double weightDiff = calculateWeightDifference(metrics);
        if (weightDiff != 0) {
            return String.format(getString(R.string.recommendation_format), action, Math.abs(weightDiff));
        }
        return "";
    }

    private double calculateWeightDifference(UserMetricsEntity metrics) {
        double heightInMeters = metrics.getHeight() / 100.0;
        double currentWeight = metrics.getWeight();
        double idealWeight = 22.0 * (heightInMeters * heightInMeters); // Using ideal BMI of 22
        return currentWeight - idealWeight;
    }

    private void updateBmiProgressBar(double bmi, int color) {
        LayerDrawable progressBarDrawable = (LayerDrawable) bmiProgressBar.getProgressDrawable().mutate();
        Drawable progressDrawable = progressBarDrawable.findDrawableByLayerId(android.R.id.progress);
        if (progressDrawable != null) {
            progressDrawable.setTint(color);
        }

        // Map BMI to a 0-100 scale for the progress bar
        int progress = (int) Math.min(Math.max(((bmi - 10) / (40 - 10)) * 100, 0), 100);
        bmiProgressBar.setProgress(progress);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}