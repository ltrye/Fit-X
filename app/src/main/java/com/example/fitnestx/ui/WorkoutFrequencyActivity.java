package com.example.fitnestx.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnestx.MainActivity;
import com.example.fitnestx.R;
import com.example.fitnestx.data.entity.WorkoutPlanEntity;
import com.example.fitnestx.data.repository.WorkoutPlanRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkoutFrequencyActivity extends AppCompatActivity {

    private WorkoutPlanRepository workoutPlanRepository;
    private ExecutorService executorService;

    // UI Components
    private ImageButton btnBack;
    private ImageButton btnDecreaseSlots;
    private ImageButton btnIncreaseSlots;
    private TextView tvSlotsCount;
    private Button btnAction;
    private LinearLayout layoutFrequencySelection;
    private LinearLayout layoutWarningMessage;

    // State variables
    private int selectedSlots = 3;
    private boolean isShowingWarning = false;

    // SharedPreferences constants
    private static final String PREF_NAME = "FitnestX";
    private static final String KEY_SURVEY_COMPLETED = "survey_completed";
    private static final String KEY_WORKOUT_FREQUENCY = "workout_frequency";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_frequency);

        // Initialize repository and executor
        workoutPlanRepository = new WorkoutPlanRepository(this);
        executorService = Executors.newSingleThreadExecutor();

        initViews();
        setupClickListeners();
        updateSlotsDisplay();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnDecreaseSlots = findViewById(R.id.btn_decrease_slots);
        btnIncreaseSlots = findViewById(R.id.btn_increase_slots);
        tvSlotsCount = findViewById(R.id.tv_slots_count);
        btnAction = findViewById(R.id.btn_action);
        layoutFrequencySelection = findViewById(R.id.layout_frequency_selection);
        layoutWarningMessage = findViewById(R.id.layout_warning_message);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            if (isShowingWarning) {
                // Go back to frequency selection
                showFrequencySelection();
            } else {
                // Go back to previous activity
                finish();
            }
        });

        btnDecreaseSlots.setOnClickListener(v -> {
            if (selectedSlots > 1) {
                selectedSlots--;
                updateSlotsDisplay();
            }
        });

        btnIncreaseSlots.setOnClickListener(v -> {
            if (selectedSlots < 7) {
                selectedSlots++;
                updateSlotsDisplay();
            }
        });

        btnAction.setOnClickListener(v -> {
            if (isShowingWarning) {
                // Save frequency and proceed to next screen
                saveWorkoutPlanToDatabase();
            } else {
                // Show warning message
                showWarningMessage();
            }
        });
    }

    private void updateSlotsDisplay() {
        tvSlotsCount.setText(String.valueOf(selectedSlots));

        // Enable/disable buttons based on limits
        btnDecreaseSlots.setEnabled(selectedSlots > 1);
        btnIncreaseSlots.setEnabled(selectedSlots < 7);

        // Update button opacity
        btnDecreaseSlots.setAlpha(selectedSlots > 1 ? 1.0f : 0.5f);
        btnIncreaseSlots.setAlpha(selectedSlots < 7 ? 1.0f : 0.5f);
    }

    private void showFrequencySelection() {
        layoutFrequencySelection.setVisibility(View.VISIBLE);
        layoutWarningMessage.setVisibility(View.GONE);
        btnAction.setText("Confirm");
        isShowingWarning = false;
    }

    private void showWarningMessage() {
        layoutFrequencySelection.setVisibility(View.GONE);
        layoutWarningMessage.setVisibility(View.VISIBLE);
        btnAction.setText("Next");
        isShowingWarning = true;
    }

    private void saveWorkoutPlanToDatabase() {
        // Get user ID
        int userId = getCurrentUserId();
        if (userId == -1) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            Log.e("WorkoutFrequencyActivity", "Invalid userId");
            return;
        }

        // Run database operations on background thread
        executorService.execute(() -> {
            try {
                // Get current date as start date
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String startDate = dateFormat.format(new Date());

                // Create WorkoutPlanEntity
                WorkoutPlanEntity workoutPlan = new WorkoutPlanEntity(
                        0, // planId is auto-generated
                        userId,
                        startDate,
                        "12", // Default 12 weeks duration - you can modify this as needed
                        selectedSlots, // daysPerWeek - this is where we save the frequency
                        true // isActive - set to true for the new plan
                );

                // Save to Room database
                workoutPlanRepository.insertWorkoutPlan(workoutPlan);

                // Save frequency to SharedPreferences as well (for backup/quick access)
                saveWorkoutFrequencyToPrefs();

                // Mark survey as completed (same as GoalSelectionActivity)
                markSurveyCompleted();

                // Navigate to MainActivity on UI thread
                runOnUiThread(() -> {
                    Toast.makeText(this, "Workout plan created successfully!", Toast.LENGTH_SHORT).show();
                    proceedToNextScreen();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Log.e("WorkoutFrequencyActivity", "Failed to save workout plan", e);
                    Toast.makeText(this, "Error saving workout plan: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void saveWorkoutFrequencyToPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_WORKOUT_FREQUENCY, selectedSlots);
        editor.apply();
    }

    private void markSurveyCompleted() {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(KEY_SURVEY_COMPLETED, true);
        editor.apply();
    }

    private void proceedToNextScreen() {
        // Navigate to MainActivity
        Intent intent = new Intent(WorkoutFrequencyActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            Log.e("WorkoutFrequencyActivity", "No userId found in SharedPreferences");
        }
        return userId;
    }

    @Override
    public void onBackPressed() {
        if (isShowingWarning) {
            showFrequencySelection();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
