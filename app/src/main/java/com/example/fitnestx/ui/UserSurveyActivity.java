package com.example.fitnestx.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnestx.MainActivity;
import com.example.fitnestx.R;
import com.example.fitnestx.data.entity.UserEntity;
import com.example.fitnestx.data.repository.UserRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserSurveyActivity extends AppCompatActivity {

    private Spinner spinnerGender;
    private LinearLayout layoutDateBirth;
    private TextView tvDateBirth;
    private EditText etWeight;
    private EditText etHeight;
    private Button btnNext;
    private UserRepository userRepository;
    private ExecutorService executorService;

    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;

    // SharedPreferences constants
    private static final String PREF_NAME = "FitnestX";
    private static final String KEY_SURVEY_COMPLETED = "survey_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_survey);

        // Initialize repository and executor
        userRepository = new UserRepository(this);
        executorService = Executors.newSingleThreadExecutor();

        // Check if survey is already completed
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        if (pref.getBoolean(KEY_SURVEY_COMPLETED, false)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        initViews();
        setupGenderSpinner();
        setupDatePicker();
        setupNextButton();

        selectedDate = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    private void initViews() {
        spinnerGender = findViewById(R.id.spinner_gender);
        layoutDateBirth = findViewById(R.id.layout_date_birth);
        tvDateBirth = findViewById(R.id.tv_date_birth);
        etWeight = findViewById(R.id.et_weight);
        etHeight = findViewById(R.id.et_height);
        btnNext = findViewById(R.id.btn_next);
    }

    private void setupGenderSpinner() {
        String[] genderOptions = {"Choose Gender", "Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genderOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void setupDatePicker() {
        layoutDateBirth.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    String formattedDate = dateFormat.format(selectedDate.getTime());
                    tvDateBirth.setText(formattedDate);
                    tvDateBirth.setTextColor(getResources().getColor(android.R.color.black));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void setupNextButton() {
        btnNext.setOnClickListener(v -> {
            if (validateInputs()) {
                saveUserProfile();
            }
        });
    }

    private boolean validateInputs() {
        if (spinnerGender.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (tvDateBirth.getText().toString().equals("Date of Birth")) {
            Toast.makeText(this, "Please select your date of birth", Toast.LENGTH_SHORT).show();
            return false;
        }
        String weight = etWeight.getText().toString().trim();
        if (weight.isEmpty()) {
            Toast.makeText(this, "Please enter your weight", Toast.LENGTH_SHORT).show();
            etWeight.requestFocus();
            return false;
        }
        try {
            double weightValue = Double.parseDouble(weight);
            if (weightValue <= 0 || weightValue > 500) {
                Toast.makeText(this, "Please enter a valid weight (1-500 kg)", Toast.LENGTH_SHORT).show();
                etWeight.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid weight", Toast.LENGTH_SHORT).show();
            etWeight.requestFocus();
            return false;
        }
        String height = etHeight.getText().toString().trim();
        if (height.isEmpty()) {
            Toast.makeText(this, "Please enter your height", Toast.LENGTH_SHORT).show();
            etHeight.requestFocus();
            return false;
        }
        try {
            double heightValue = Double.parseDouble(height);
            if (heightValue <= 0 || heightValue > 300) {
                Toast.makeText(this, "Please enter a valid height (1-300 cm)", Toast.LENGTH_SHORT).show();
                etHeight.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid height", Toast.LENGTH_SHORT).show();
            etHeight.requestFocus();
            return false;
        }
        return true;
    }

    private void saveUserProfile() {
        int userId = getCurrentUserId();
        if (userId == -1) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
                Log.e("UserSurveyActivity", "Invalid userId");
            });
            return;
        }

        // Map gender to boolean
        String genderStr = spinnerGender.getSelectedItem().toString();
        boolean gender = genderStr.equals("Male");

        // Calculate age from date of birth
        Calendar today = Calendar.getInstance();
        int calculatedAge = today.get(Calendar.YEAR) - selectedDate.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < selectedDate.get(Calendar.DAY_OF_YEAR)) {
            calculatedAge--;
        }

        // Create a final variable to use inside the lambda
        final int finalAge = calculatedAge;

        // Run database operations on background thread
        executorService.execute(() -> {
            try {
                // Get user from database
                UserEntity user = userRepository.getUserById(userId);
                if (user == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
                        Log.e("UserSurveyActivity", "User not found for userId: " + userId);
                    });
                    return;
                }

                // Update user data
                user.setGender(gender);
                user.setAge(finalAge);
                userRepository.updateUser(user);

                // Save to SharedPreferences (optional)
                SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("gender", genderStr);
                editor.putInt("age", finalAge);
                editor.putString("weight", etWeight.getText().toString());
                editor.putString("height", etHeight.getText().toString());
                editor.apply();

                // Navigate to GoalSelectionActivity
                runOnUiThread(() -> {
                    Intent intent = new Intent(UserSurveyActivity.this, GoalSelectionActivity.class);
                    intent.putExtra("weight", etWeight.getText().toString());
                    intent.putExtra("height", etHeight.getText().toString());
                    startActivity(intent);
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Log.e("UserSurveyActivity", "Failed to save user profile", e);
                    Toast.makeText(this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            Log.e("UserSurveyActivity", "No userId found in SharedPreferences");
        }
        return userId;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    @Override
    public void onBackPressed() {
        // If the user is on this screen, they are in the middle of onboarding.
        // Pressing back should exit the application to prevent an infinite loop.
        // This stops them from going back to LoginActivity, which would re-trigger the check.
        super.onBackPressed();
        finishAffinity(); // Finishes this activity and all parent activities, effectively closing the app.
    }
}
