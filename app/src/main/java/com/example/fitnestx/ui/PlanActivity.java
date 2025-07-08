package com.example.fitnestx.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnestx.R;
import com.example.fitnestx.adapters.GoalAdapter;
import com.example.fitnestx.data.entity.UserEntity;
import com.example.fitnestx.data.entity.UserMetricsEntity;
import com.example.fitnestx.data.entity.WorkoutPlanEntity;
import com.example.fitnestx.data.entity.WorkoutSessionEntity;
import com.example.fitnestx.data.repository.SessionExerciseRepository;
import com.example.fitnestx.data.repository.UserMetricsRepository;
import com.example.fitnestx.data.repository.UserRepository;
import com.example.fitnestx.data.repository.WorkoutPlanRepository;
import com.example.fitnestx.data.repository.WorkoutSessionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlanActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private GoalAdapter goalAdapter;
    private List<WorkoutSessionEntity> goalList;
    private TextView greetingText,goal;
    private SessionExerciseRepository sessionExerciseRepository;
    private WorkoutSessionRepository workoutSessionRepository;
    private UserRepository userRepository;
    private UserMetricsRepository userMetricsRepository;
    private WorkoutPlanRepository workoutPlanRepository;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
        sessionExerciseRepository = new SessionExerciseRepository(this);
        workoutSessionRepository = new WorkoutSessionRepository(this);
        workoutPlanRepository = new WorkoutPlanRepository(this);
        goalList = new ArrayList<>();
        userRepository = new UserRepository(this);
        userMetricsRepository = new UserMetricsRepository(this);

        initViews();
        setupRecyclerView();

    }

    private void initViews() {
        greetingText = findViewById(R.id.greeting_text);
        recyclerView = findViewById(R.id.recycler_view);
        goal = findViewById(R.id.goal);
        int userId = getCurrentUserId();
        if (userId == -1) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            Log.e("WorkoutFrequencyActivity", "Invalid userId");
            return;
        }
        // Chạy trong background thread
        executorService.execute(() -> {
            UserEntity user = userRepository.getUserById(userId);
            UserMetricsEntity userMetrics = userMetricsRepository.getUserMetricByUserId(userId);
            if (user != null) {
                // Update UI trên UI thread
                runOnUiThread(() -> {
                    greetingText.setText("Hello, " + user.getName());
                    goal.setText(userMetrics.getGoal());
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupRecyclerView() {
        int finalPlanId;
        int planIdFromIntent = getIntent().getIntExtra("planId", -1);

        int userId = getCurrentUserId();
        if (userId == -1) return;

        executorService.execute(() -> {
            int planId = planIdFromIntent;
            if (planId == -1) {
                WorkoutPlanEntity plan = workoutPlanRepository.getWorkoutPlansByUserId(userId);
                if (plan != null) {
                    planId = plan.getPlanId();
                } else {
                    int finalPlanId1 = planId;
                    runOnUiThread(() -> Toast.makeText(this, "No workout plan found", Toast.LENGTH_SHORT).show());
                    return;
                }
            }

            List<WorkoutSessionEntity> sessions = workoutSessionRepository.getWorkoutSessionsByPlanId(planId);
            sessions.sort((s1, s2) -> {
                int day1 = extractDayNumber(s1.getDate());
                int day2 = extractDayNumber(s2.getDate());
                return Integer.compare(day1, day2);
            });

            runOnUiThread(() -> {
                goalAdapter = new GoalAdapter(sessions, this, sessionExerciseRepository);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(goalAdapter);
            });
        });
    }



    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            Log.e("WorkoutFrequencyActivity", "No userId found in SharedPreferences");
        }
        return userId;
    }
    private int extractDayNumber(String label) {
        // Giả sử format luôn là "Ngày <số>"
        try {
            return Integer.parseInt(label.replaceAll("[^0-9]", "").trim());
        } catch (NumberFormatException e) {
            return 0; // fallback nếu có lỗi
        }
    }

}
