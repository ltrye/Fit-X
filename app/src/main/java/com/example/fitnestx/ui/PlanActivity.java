package com.example.fitnestx.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
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
import com.example.fitnestx.fragments.TopMenuFragment;

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
    public static final int REQUEST_CODE_EXERCISE = 1001; // Bất kỳ số nào bạn chọn
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
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_top_menu, new TopMenuFragment());
        transaction.commit();
        initViews();
        setupRecyclerView();

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        setupRecyclerView(); // Luôn reload dữ liệu khi màn hình được resume
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EXERCISE && resultCode == RESULT_OK && data != null) {
            int sessionId = data.getIntExtra("sessionId", -1);
            if (sessionId != -1) {
                updateSingleSession(sessionId);
            }
        }
    }
    private void updateSingleSession(int sessionId) {
        executorService.execute(() -> {
            WorkoutSessionEntity updatedSession = workoutSessionRepository.getWorkoutSessionById(sessionId);
            if (updatedSession == null) return;

            runOnUiThread(() -> {
                for (int i = 0; i < goalAdapter.getItemCount(); i++) {
                    WorkoutSessionEntity session = goalAdapter.getItem(i);
                    if (session.getSessionId() == sessionId) {
                        goalAdapter.updateItem(i, updatedSession);
                        break;
                    }
                }
            });
        });
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
                if (goalAdapter == null) {
                    goalAdapter = new GoalAdapter(sessions, this, sessionExerciseRepository);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(goalAdapter);
                } else {
                    goalAdapter.updateData(sessions); // ← Sử dụng được vì bạn đã thêm hàm này
                }
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
    private int extractDayNumber(String date) {
        if (date == null) return 0;
        try {
            date = date.trim().toLowerCase().replace("ngày", "").trim();
            return Integer.parseInt(date);
        } catch (NumberFormatException e) {
            return 0;
        }
    }


}
