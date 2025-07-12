package com.example.fitnestx.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button; // Import Button
import android.widget.LinearLayout;
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
    private TextView greetingText, goal, tvBMIValue, tvBMIStatus;
    private LinearLayout bmiCard;
    private Button btnCompletePlan; // Declare the new button

    private SessionExerciseRepository sessionExerciseRepository;
    private WorkoutSessionRepository workoutSessionRepository;
    private UserRepository userRepository;
    private UserMetricsRepository userMetricsRepository;
    private WorkoutPlanRepository workoutPlanRepository;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    public static final int REQUEST_CODE_EXERCISE = 1001;
    public static final int REQUEST_CODE_COMPLETION = 1002;

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


        // Thêm nút để truy cập danh sách bài tập đơn lẻ
        Button btnIndividualExercises = findViewById(R.id.btn_individual_exercises);
        btnIndividualExercises.setOnClickListener(v -> {
            Intent intent = new Intent(PlanActivity.this, MuscleGroupListActivity.class);
            startActivity(intent);
        });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_top_menu, new TopMenuFragment());
        transaction.commit();

        initViews();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to PlanActivity
        setupRecyclerView();
        // Delay 200–300ms rồi mới update nút để chờ dữ liệu load xong
        new android.os.Handler().postDelayed(() -> {
            updateCompletePlanButtonState();
        }, 1000);
    }
    private void setupRecyclerViewWithCallback(Runnable callback) {
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
                    goalAdapter.updateData(sessions);
                }

                // 🔁 Gọi callback (updateCompletePlanButtonState) sau khi dữ liệu load xong
                if (callback != null) callback.run();
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EXERCISE && resultCode == RESULT_OK && data != null) {
            int sessionId = data.getIntExtra("sessionId", -1);
            if (sessionId != -1) {
                updateSingleSession(sessionId);
                // updateCompletePlanButtonState() will be called in onResume
            }
        } else if (requestCode == REQUEST_CODE_COMPLETION) {
            if (resultCode == RESULT_OK) {
                // If WorkoutCompletionActivity returned OK, it means a reset/regeneration happened
                setupRecyclerView(); // Refresh UI
                updateCompletePlanButtonState(); // Update button state after reset
            }
            // If RESULT_CANCELED, it means user chose to continue, no refresh needed
        }
    }

    private void updateCompletePlanButtonState() {
        int userId = getCurrentUserId();
        if (userId == -1) {
            runOnUiThread(() -> btnCompletePlan.setEnabled(false)); // Disable if no user
            return;
        }

        executorService.execute(() -> {
            WorkoutPlanEntity currentPlan = workoutPlanRepository.getWorkoutPlansByUserId(userId);
            if (currentPlan == null) {
                runOnUiThread(() -> btnCompletePlan.setEnabled(false));
                return;
            }

            List<WorkoutSessionEntity> sessions = workoutSessionRepository.getWorkoutSessionsByPlanId(currentPlan.getPlanId());

            boolean allSessionsCompleted = true;
            if (sessions.isEmpty()) {
                allSessionsCompleted = false; // No sessions means nothing to complete
            } else {
                for (WorkoutSessionEntity session : sessions) {
                    if (!session.getIsCompleted()) {
                        allSessionsCompleted = false;
                        break;
                    }
                }
            }

            boolean finalAllSessionsCompleted = allSessionsCompleted;
            runOnUiThread(() -> btnCompletePlan.setEnabled(finalAllSessionsCompleted));
        });

    }

    private void updateSingleSession(int sessionId) {
        executorService.execute(() -> {
            WorkoutSessionEntity updatedSession = workoutSessionRepository.getWorkoutSessionById(sessionId);
            if (updatedSession == null) return;

            runOnUiThread(() -> {
                for (int i = 0; i < goalAdapter.getItemCount(); i++) {
                    WorkoutSessionEntity session = goalAdapter.getItem(i);
                    if (session.getSessionId() == updatedSession.getSessionId()) {
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
       // goal = findViewById(R.id.goal);
        bmiCard = findViewById(R.id.bmi_card);
        tvBMIValue = findViewById(R.id.tv_bmi_value);
        tvBMIStatus = findViewById(R.id.tv_bmi_status);
        btnCompletePlan = findViewById(R.id.btn_complete_plan); // Initialize the new button

        // Set click listener cho BMI card
        bmiCard.setOnClickListener(v -> {
            Intent intent = new Intent(PlanActivity.this, BMIDetailActivity.class);
            startActivity(intent);
        });
        updateCompletePlanButtonState();
        // Set click listener for the new "Complete Plan" button
        btnCompletePlan.setOnClickListener(v -> {
            int userId = getCurrentUserId();
            if (userId == -1) {
                Toast.makeText(PlanActivity.this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            executorService.execute(() -> {
                WorkoutPlanEntity currentPlan = workoutPlanRepository.getWorkoutPlansByUserId(userId);
                if (currentPlan != null) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(PlanActivity.this, WorkoutCompletionActivity.class);
                        intent.putExtra("planId", currentPlan.getPlanId());
                        intent.putExtra("userId", userId);
                        startActivityForResult(intent, REQUEST_CODE_COMPLETION);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(PlanActivity.this, "No workout plan found to complete.", Toast.LENGTH_SHORT).show());
                }
            });
        });


        int userId = getCurrentUserId();
        if (userId == -1) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            Log.e("PlanActivity", "Invalid userId");
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
                    if (userMetrics != null) {
                       // goal.setText(userMetrics.getGoal());
                        setupBMICard(userMetrics);
                    }
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupBMICard(UserMetricsEntity userMetrics) {
        double bmi = userMetrics.getBmi();
        tvBMIValue.setText(String.format("%.1f", bmi));

        // Xác định trạng thái BMI và màu sắc
        String status;
        int statusColor;

        if (bmi < 18.5) {
            status = "Thiếu cân";
            statusColor = getResources().getColor(android.R.color.holo_blue_light);
        } else if (bmi < 25) {
            status = "Bình thường";
            statusColor = getResources().getColor(android.R.color.holo_green_light);
        } else if (bmi < 30) {
            status = "Thừa cân";
            statusColor = getResources().getColor(android.R.color.holo_orange_light);
        } else {
            status = "Béo phì";
            statusColor = getResources().getColor(android.R.color.holo_red_light);
        }

        tvBMIStatus.setText(status);
        tvBMIStatus.setTextColor(statusColor);
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
                    goalAdapter.updateData(sessions);
//                    updateCompletePlanButtonState();
                }
           //     updateCompletePlanButtonState(); // Update button state after RecyclerView is set up
            });
        });
    }

    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            Log.e("PlanActivity", "No userId found in SharedPreferences");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
