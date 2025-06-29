package com.example.fitnestx.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UserSurveyActivity extends AppCompatActivity {

    private Spinner spinnerGender;
    private LinearLayout layoutDateBirth;
    private TextView tvDateBirth;
    private EditText etWeight;
    private EditText etHeight;
    private Button btnNext;

    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;

    // SharedPreferences constants
    private static final String PREF_NAME = "MyAppPrefs";
    private static final String KEY_SURVEY_COMPLETED = "survey_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_survey);

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

        // Set maximum date to today (user can't be born in the future)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Set minimum date to 100 years ago
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private void setupNextButton() {
        btnNext.setOnClickListener(v -> {
            if (validateInputs()) {
                // Save user data
                saveUserData();

                // Mark survey as completed
                markSurveyCompleted();

                // Navigate to MainActivity
                Intent intent = new Intent(UserSurveyActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean validateInputs() {
        // Check gender selection
        if (spinnerGender.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check date of birth
        if (tvDateBirth.getText().toString().equals("Date of Birth")) {
            Toast.makeText(this, "Please select your date of birth", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check weight
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

        // Check height
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

    private void saveUserData() {
        // Get the selected values
        String gender = spinnerGender.getSelectedItem().toString();
        String dateOfBirth = tvDateBirth.getText().toString();
        String weight = etWeight.getText().toString();
        String height = etHeight.getText().toString();

        // Save to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("gender", gender);
        editor.putString("dateOfBirth", dateOfBirth);
        editor.putString("weight", weight);
        editor.putString("height", height);
        editor.apply();

        Toast.makeText(this, "Profile completed successfully!", Toast.LENGTH_SHORT).show();
    }

    private void markSurveyCompleted() {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(KEY_SURVEY_COMPLETED, true);
        editor.apply();
    }
}
