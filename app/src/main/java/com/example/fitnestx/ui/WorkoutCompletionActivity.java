package com.example.fitnestx.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnestx.R;
import com.example.fitnestx.Helpers.GeneratePlan;
import com.example.fitnestx.data.entity.UserEntity;
import com.example.fitnestx.data.entity.UserMetricsEntity;
import com.example.fitnestx.data.entity.WorkoutPlanEntity;
import com.example.fitnestx.data.entity.WorkoutSessionEntity;
import com.example.fitnestx.data.repository.ExerciseRepository;
import com.example.fitnestx.data.repository.MuscleGroupRepository;
import com.example.fitnestx.data.repository.SessionExerciseRepository;
import com.example.fitnestx.data.repository.UserMetricsRepository;
import com.example.fitnestx.data.repository.UserRepository;
import com.example.fitnestx.data.repository.WorkoutPlanRepository;
import com.example.fitnestx.data.repository.WorkoutSessionRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkoutCompletionActivity extends AppCompatActivity {

    private Button btnContinue, btnResetStatus, btnRegenerateExercises;
    private int planId;
    private int userId;

    private WorkoutSessionRepository workoutSessionRepository;
    private SessionExerciseRepository sessionExerciseRepository;
    private UserMetricsRepository userMetricsRepository;
    private WorkoutPlanRepository workoutPlanRepository;
    private ExerciseRepository exerciseRepository;
    private MuscleGroupRepository muscleGroupRepository;
    private UserRepository userRepository;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_completion);

        planId = getIntent().getIntExtra("planId", -1);
        userId = getIntent().getIntExtra("userId", -1);

        if (planId == -1 || userId == -1) {
            Toast.makeText(this, "Error: Invalid plan or user ID.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        workoutSessionRepository = new WorkoutSessionRepository(this);
        sessionExerciseRepository = new SessionExerciseRepository(this);
        userMetricsRepository = new UserMetricsRepository(this);
        workoutPlanRepository = new WorkoutPlanRepository(this);
        exerciseRepository = new ExerciseRepository(this);
        muscleGroupRepository = new MuscleGroupRepository(this);
        userRepository = new UserRepository(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnContinue = findViewById(R.id.btn_continue_current_plan);
        btnResetStatus = findViewById(R.id.btn_reset_status);
        btnRegenerateExercises = findViewById(R.id.btn_regenerate_exercises);
    }

    private void setupListeners() {
        btnContinue.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED); // Indicate no change needed
            finish();
        });

        btnResetStatus.setOnClickListener(v -> {
            resetWorkoutStatus();
        });

        btnRegenerateExercises.setOnClickListener(v -> {
            showFrequencySelectionDialog();
        });
    }

    private void resetWorkoutStatus() {
        executorService.execute(() -> {
            // Update all sessions' completion status to false
            workoutSessionRepository.updateAllSessionsCompletionStatus(planId, false);
            // Update all exercises' marked status to false for this plan
            sessionExerciseRepository.updateAllExercisesMarkedStatusForPlan(planId, false);

            runOnUiThread(() -> {
                Toast.makeText(WorkoutCompletionActivity.this, "Trạng thái hoàn thành đã được đặt lại!",
                        Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK); // Indicate successful reset
                finish();
            });
        });
    }

    private void regenerateWorkoutExercises() {
        executorService.execute(() -> {
            UserMetricsEntity userMetrics = userMetricsRepository.getUserMetricByUserId(userId);
            WorkoutPlanEntity workoutPlan = workoutPlanRepository.getWorkoutPlanById(planId);
            UserEntity user = userRepository.getUserById(userId);

            if (userMetrics == null || workoutPlan == null || user == null) {
                runOnUiThread(() -> Toast.makeText(WorkoutCompletionActivity.this,
                        "Không thể tạo lại bài tập: Thiếu thông tin người dùng hoặc kế hoạch.", Toast.LENGTH_LONG)
                        .show());
                return;
            }

            double bmi = userMetrics.getBmi();
            String goal = userMetrics.getGoal();
            boolean gender = user.getGender(); // Get gender from UserEntity
            int daysPerWeek = workoutPlan.getDaysPerWeek();

            // Delete existing session exercises for this plan
            sessionExerciseRepository.deleteAllSessionExercisesForPlan(planId);

            // Reset session completion status before regenerating exercises
            workoutSessionRepository.updateAllSessionsCompletionStatus(planId, false);

            // Re-generate exercises for existing sessions
            GeneratePlan generator = new GeneratePlan(
                    planId, bmi, goal, gender, daysPerWeek,
                    exerciseRepository, muscleGroupRepository,
                    workoutSessionRepository, sessionExerciseRepository,
                    workoutPlanRepository);
            generator.regeneratePlanExercises(); // Call the new method for regeneration

            runOnUiThread(() -> {
                Toast.makeText(WorkoutCompletionActivity.this, "Bài tập đã được tạo lại!", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK); // Indicate successful regeneration
                finish();
            });
        });
    }

    private void regenerateWorkoutExercisesWithNewFrequency(int daysPerWeek) {
        executorService.execute(() -> {
            UserMetricsEntity userMetrics = userMetricsRepository.getUserMetricByUserId(userId);
            WorkoutPlanEntity workoutPlan = workoutPlanRepository.getWorkoutPlanById(planId);
            UserEntity user = userRepository.getUserById(userId);

            if (userMetrics == null || workoutPlan == null || user == null) {
                runOnUiThread(() -> Toast.makeText(WorkoutCompletionActivity.this,
                        "Không thể tạo lại bài tập: Thiếu thông tin người dùng hoặc kế hoạch.", Toast.LENGTH_LONG)
                        .show());
                return;
            }

            double bmi = userMetrics.getBmi();
            String goal = userMetrics.getGoal();
            boolean gender = user.getGender(); // Get gender from UserEntity

            // Delete existing session exercises for this plan
            sessionExerciseRepository.deleteAllSessionExercisesForPlan(planId);

            // Reset session completion status before regenerating exercises
            workoutSessionRepository.updateAllSessionsCompletionStatus(planId, false);

            // Update the days per week in the workout plan
            workoutPlan.setDaysPerWeek(daysPerWeek);
            workoutPlanRepository.updateWorkoutPlan(workoutPlan);

            // Delete all workout sessions for this plan
            workoutSessionRepository.deleteAllWorkoutSessionForPlanId(planId);

            // Create new workout sessions
            for (int day = 1; day <= daysPerWeek; day++) {
                WorkoutSessionEntity session = new WorkoutSessionEntity(planId, "Ngày " + day, 1, false);
                workoutSessionRepository.insertWorkoutSession(session);
            }

            // Re-generate exercises for existing sessions
            GeneratePlan generator = new GeneratePlan(
                    planId, bmi, goal, gender, daysPerWeek,
                    exerciseRepository, muscleGroupRepository,
                    workoutSessionRepository, sessionExerciseRepository,
                    workoutPlanRepository);
            generator.regeneratePlanExercises(); // Call the new method for regeneration

            runOnUiThread(() -> {
                Toast.makeText(WorkoutCompletionActivity.this, "Bài tập đã được tạo lại!", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK); // Indicate successful regeneration
                finish();
            });
        });
    }

    /**
     * Show dialog to select workout frequency before regenerating exercises
     */
    private void showFrequencySelectionDialog() {
        // Get current workout plan to show current frequency
        executorService.execute(() -> {
            WorkoutPlanEntity currentPlan = workoutPlanRepository.getWorkoutPlanById(planId);
            int currentFrequency = (currentPlan != null) ? currentPlan.getDaysPerWeek() : 3;

            runOnUiThread(() -> {
                displayFrequencyDialog(currentFrequency);
            });
        });
    }

    /**
     * Display the frequency selection dialog with current frequency
     */
    private void displayFrequencyDialog(int currentFrequency) {
        // Create dialog layout
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 50, 50, 50);

        // Title
        TextView titleText = new TextView(this);
        titleText.setText("Bạn có muốn thay đổi số lượng buổi tập của tuần tới?");
        titleText.setTextSize(16);
        titleText.setPadding(0, 0, 0, 30);

        // Weekly Slots Display Card
        LinearLayout slotsCard = new LinearLayout(this);
        slotsCard.setOrientation(LinearLayout.HORIZONTAL);
        slotsCard.setPadding(16, 16, 16, 16);
        slotsCard.setBackgroundResource(R.drawable.goal_card_background);

        TextView weeklyLabel = new TextView(this);
        weeklyLabel.setText("Hàng tuần, ");
        weeklyLabel.setTextColor(getResources().getColor(android.R.color.white));
        weeklyLabel.setTextSize(14);

        TextView slotsDisplay = new TextView(this);
        slotsDisplay.setText(currentFrequency + " buổi");
        slotsDisplay.setTextColor(getResources().getColor(android.R.color.white));
        slotsDisplay.setTextSize(14);
        slotsDisplay.setTypeface(null, android.graphics.Typeface.BOLD);

        slotsCard.addView(weeklyLabel);
        slotsCard.addView(slotsDisplay);

        // Slots Label
        TextView slotsLabel = new TextView(this);
        slotsLabel.setText("Buổi tập");
        slotsLabel.setTextSize(16);
        slotsLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        slotsLabel.setPadding(0, 30, 0, 20);

        // Slots Selector
        LinearLayout slotsSelector = new LinearLayout(this);
        slotsSelector.setOrientation(LinearLayout.HORIZONTAL);
        slotsSelector.setGravity(android.view.Gravity.CENTER);

        ImageButton btnDecrease = new ImageButton(this);
        btnDecrease.setImageResource(R.drawable.ic_minus);
        btnDecrease.setBackgroundResource(R.drawable.button_background);
        btnDecrease.setLayoutParams(new LinearLayout.LayoutParams(120, 120));

        TextView slotsCount = new TextView(this);
        slotsCount.setText(String.valueOf(currentFrequency));
        slotsCount.setTextSize(32);
        slotsCount.setTypeface(null, android.graphics.Typeface.BOLD);
        slotsCount.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(150,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        countParams.setMargins(60, 0, 60, 0);
        slotsCount.setLayoutParams(countParams);

        ImageButton btnIncrease = new ImageButton(this);
        btnIncrease.setImageResource(R.drawable.ic_plus);
        btnIncrease.setBackgroundResource(R.drawable.button_background);
        btnIncrease.setLayoutParams(new LinearLayout.LayoutParams(120, 120));

        // Button click listeners for frequency adjustment
        btnDecrease.setOnClickListener(v -> {
            int current = Integer.parseInt(slotsCount.getText().toString());
            if (current > 1) {
                int newValue = current - 1;
                slotsCount.setText(String.valueOf(newValue));
                slotsDisplay.setText(newValue + " buổi");
            }
        });

        btnIncrease.setOnClickListener(v -> {
            int current = Integer.parseInt(slotsCount.getText().toString());
            if (current < 7) {
                int newValue = current + 1;
                slotsCount.setText(String.valueOf(newValue));
                slotsDisplay.setText(newValue + " buổi");
            }
        });

        slotsSelector.addView(btnDecrease);
        slotsSelector.addView(slotsCount);
        slotsSelector.addView(btnIncrease);

        // Add views to dialog layout
        dialogLayout.addView(titleText);
        dialogLayout.addView(slotsCard);
        dialogLayout.addView(slotsLabel);
        dialogLayout.addView(slotsSelector);

        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogLayout)
                .setCancelable(false)
                .create();

        // Create custom buttons
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setPadding(0, 30, 0, 0);

        Button skipButton = new Button(this);
        skipButton.setText("Skip");
        skipButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        skipButton.setBackgroundResource(R.drawable.button_background);
        skipButton.setTextColor(getResources().getColor(android.R.color.white));

        Button updateButton = new Button(this);
        updateButton.setText("Update");
        updateButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        updateButton.setBackgroundResource(R.drawable.button_background);
        updateButton.setTextColor(getResources().getColor(android.R.color.white));

        // Add margin between buttons
        LinearLayout.LayoutParams updateParams = (LinearLayout.LayoutParams) updateButton.getLayoutParams();
        updateParams.setMargins(20, 0, 0, 0);
        updateButton.setLayoutParams(updateParams);

        buttonLayout.addView(skipButton);
        buttonLayout.addView(updateButton);
        dialogLayout.addView(buttonLayout);

        // Skip button click listener - use current frequency
        skipButton.setOnClickListener(v -> {
            dialog.dismiss();
            regenerateWorkoutExercises();
        });

        // Update button click listener - use selected frequency
        updateButton.setOnClickListener(v -> {
            int selectedFrequency = Integer.parseInt(slotsCount.getText().toString());
            dialog.dismiss();
            regenerateWorkoutExercisesWithNewFrequency(selectedFrequency);
        });

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
