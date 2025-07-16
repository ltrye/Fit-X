package com.example.fitnestx.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnestx.R;
import com.example.fitnestx.Helpers.GeneratePlan;
import com.example.fitnestx.data.entity.UserEntity;
import com.example.fitnestx.data.entity.UserMetricsEntity;
import com.example.fitnestx.data.entity.WorkoutPlanEntity;
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
            regenerateWorkoutExercises();
        });
    }

    private void resetWorkoutStatus() {
        executorService.execute(() -> {
            // Update all sessions' completion status to false
            workoutSessionRepository.updateAllSessionsCompletionStatus(planId, false);
            // Update all exercises' marked status to false for this plan
            sessionExerciseRepository.updateAllExercisesMarkedStatusForPlan(planId, false);

            runOnUiThread(() -> {
                Toast.makeText(WorkoutCompletionActivity.this, "Trạng thái hoàn thành đã được đặt lại!", Toast.LENGTH_SHORT).show();
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
                runOnUiThread(() -> Toast.makeText(WorkoutCompletionActivity.this, "Không thể tạo lại bài tập: Thiếu thông tin người dùng hoặc kế hoạch.", Toast.LENGTH_LONG).show());
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
                    workoutPlanRepository
            );
            generator.regeneratePlanExercises(); // Call the new method for regeneration

            runOnUiThread(() -> {
                Toast.makeText(WorkoutCompletionActivity.this, "Bài tập đã được tạo lại!", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK); // Indicate successful regeneration
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
