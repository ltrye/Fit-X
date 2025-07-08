package com.example.fitnestx.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnestx.MainActivity;
import com.example.fitnestx.R;
import com.example.fitnestx.data.entity.UserMetricsEntity;
import com.example.fitnestx.data.repository.UserMetricsRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GoalSelectionActivity extends AppCompatActivity {

    private LinearLayout cardImproveShape, cardLeanTone, cardLoseFat;
    private Button btnConfirm;
    private ImageButton btnBack;
    private String selectedGoal = "";
    private UserMetricsRepository userMetricsRepository;

    // SharedPreferences constants
    private static final String PREF_NAME = "FitnestX";
    private static final String KEY_SURVEY_COMPLETED = "survey_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_selection);

        // Initialize repository
        userMetricsRepository = new UserMetricsRepository(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        cardImproveShape = findViewById(R.id.card_improve_shape);
        cardLeanTone = findViewById(R.id.card_lean_tone);
        cardLoseFat = findViewById(R.id.card_lose_fat);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        cardImproveShape.setOnClickListener(v -> selectGoal("improve_shape", cardImproveShape));
        cardLeanTone.setOnClickListener(v -> selectGoal("lean_tone", cardLeanTone));
        cardLoseFat.setOnClickListener(v -> selectGoal("lose_fat", cardLoseFat));

        btnConfirm.setOnClickListener(v -> {
            if (!selectedGoal.isEmpty()) {
                saveGoalSelection();
                if (saveUserDataToDatabase()) {
                    Intent intent = new Intent(GoalSelectionActivity.this, WorkoutFrequencyActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                Toast.makeText(this, "Please select a goal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectGoal(String goal, LinearLayout selectedCard) {
        cardImproveShape.setBackgroundResource(R.drawable.goal_card_unselected);
        cardLeanTone.setBackgroundResource(R.drawable.goal_card_unselected);
        cardLoseFat.setBackgroundResource(R.drawable.goal_card_unselected);

        selectedCard.setBackgroundResource(R.drawable.goal_card_selected);
        selectedGoal = goal;

        btnConfirm.setEnabled(true);
        btnConfirm.setAlpha(1.0f);
    }

    private void saveGoalSelection() {
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("fitness_goal", selectedGoal);
        editor.apply();
    }

    private boolean saveUserDataToDatabase() {
        try {
            // Get data from Intent
            Intent intent = getIntent();
            String weight = intent.getStringExtra("weight");
            String height = intent.getStringExtra("height");

            // Validate Intent data
            if (weight == null || height == null) {
                Toast.makeText(this, "Error: Missing weight or height", Toast.LENGTH_SHORT).show();
                Log.e("GoalSelectionActivity", "Missing weight or height in Intent");
                return false;
            }

            // Calculate BMI
            double weightValue = Double.parseDouble(weight);
            double heightValue = Double.parseDouble(height) / 100; // Convert cm to meters
            double bmi = weightValue / (heightValue * heightValue);

            // Get timestamp
            SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String timestamp = timestampFormat.format(new Date());

            // Get user ID
            int userId = getCurrentUserId();
            if (userId == -1) {
                Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
                Log.e("GoalSelectionActivity", "Invalid userId: " + userId);
                return false;
            }

            // Create UserMetricsEntity
            UserMetricsEntity userMetrics = new UserMetricsEntity(
                    0, // metricId is auto-generated
                    userId,
                    timestamp,
                    weightValue,
                    heightValue * 100, // Store height in cm
                    21.5,
                    selectedGoal
            );

            // Save to Room database
            userMetricsRepository.insertUserMetric(userMetrics);
            Toast.makeText(this, "Profile and goal saved successfully!", Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception e) {
            Log.e("GoalSelectionActivity", "Failed to save user data to database", e);
            Toast.makeText(this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            Log.e("GoalSelectionActivity", "No userId found in SharedPreferences");
        }
        return userId;
    }
}
