package com.example.fitnestx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitnestx.data.repository.UserMetricsRepository;
import com.example.fitnestx.data.repository.WorkoutPlanRepository;
import com.example.fitnestx.ui.LoginActivity;
import com.example.fitnestx.ui.UserSurveyActivity;
import com.example.fitnestx.ui.WorkoutFrequencyActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Button btnLogout;
    private UserMetricsRepository userMetricsRepository;
    private WorkoutPlanRepository workoutPlanRepository;
    private ExecutorService executorService;
    private boolean hasInitiatedCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Initialize repositories and executor
        userMetricsRepository = new UserMetricsRepository(this);
        workoutPlanRepository = new WorkoutPlanRepository(this);
        executorService = Executors.newSingleThreadExecutor();
    }

    // This handles cases where the user returns to the activity.
    @Override
    protected void onResume() {
        super.onResume();
        // Only run the check if it hasn't been started for this activity instance.
        if (!hasInitiatedCheck) {
            hasInitiatedCheck = true; // Set the flag immediately
            checkUserOnboardingStatus();
        }
    }

    private void checkUserOnboardingStatus() {
        executorService.execute(() -> {
            int userId = getCurrentUserId();
            if (userId == -1) {
                // Not logged in, critical error, or cleared prefs. Go to Login.
                redirectTo(LoginActivity.class);
                return;
            }

            // Check 1: Does the user have metrics data? (From GoalSelectionActivity)
            boolean hasMetrics = userMetricsRepository.hasMetricsForUser(userId);
            if (!hasMetrics) {
                Log.d("MainActivity", "User #" + userId + " has no metrics. Redirecting to UserSurvey.");
                redirectTo(UserSurveyActivity.class);
                return;
            }

            // Check 2: Does the user have an active workout plan? (From WorkoutFrequencyActivity)
            boolean hasWorkoutPlan = workoutPlanRepository.hasActivePlanForUser(userId);
            if (!hasWorkoutPlan) {
                Log.d("MainActivity", "User #" + userId + " has no active plan. Redirecting to WorkoutFrequency.");
                redirectTo(WorkoutFrequencyActivity.class);
                return;
            }

            // If all checks pass, the user is fully onboarded. Load the main activity UI.
            runOnUiThread(this::setupMainActivityUI);
        });
    }

    private void setupMainActivityUI() {
        setContentView(R.layout.activity_main);
        btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> logout());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Any other UI setup for MainActivity goes here.
        Log.d("MainActivity", "User is fully onboarded. Displaying main screen.");
    }

    private void logout() {
        var gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        var mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            // Clear all relevant preferences
            SharedPreferences pref = getSharedPreferences("FitnestX", MODE_PRIVATE);
            pref.edit().clear().apply(); // Clearing all is safer on logout

            SharedPreferences authPrefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
            authPrefs.edit().clear().apply();

            // Clear the user profile cache
            SharedPreferences userProfilePrefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
            userProfilePrefs.edit().clear().apply();

            Toast.makeText(MainActivity.this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();

            redirectTo(LoginActivity.class);
        });
    }

    private void redirectTo(Class<?> activityClass) {
        runOnUiThread(() -> {
            Intent intent = new Intent(MainActivity.this, activityClass);
            // Clear the activity stack to prevent the user from navigating back to a partially loaded MainActivity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Finish MainActivity so it's not in the back stack
        });
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
