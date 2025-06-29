package com.example.fitnestx.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnestx.MainActivity;
import com.example.fitnestx.R;
import com.example.fitnestx.data.AppDatabase;
import com.example.fitnestx.data.entity.AuthProviderEntity;
import com.example.fitnestx.data.entity.UserEntity;
import com.example.fitnestx.data.repository.UserRepository;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private Button btnGoogleSignIn;

    private UserRepository userRepository;
    private GoogleSignInClient mGoogleSignInClient;

    // SharedPreferences constants
    private static final String PREF_NAME = "FitnestX";
    private static final String KEY_LOGGED_IN = "isLoggedIn";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_ID = "user_id";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_SURVEY_COMPLETED = "survey_completed";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ view
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        btnGoogleSignIn = findViewById(R.id.btn_google);

        userRepository = new UserRepository(this);

        // Cấu hình đăng nhập bằng Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Hiện/ẩn mật khẩu
        final boolean[] isPasswordVisible = {false};
        etPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableRight = 2;
                if (event.getRawX() >= (etPassword.getRight() - etPassword.getCompoundDrawables()[drawableRight].getBounds().width())) {
                    isPasswordVisible[0] = !isPasswordVisible[0];
                    if (isPasswordVisible[0]) {
                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visibility, 0);
                    } else {
                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        etPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visibility_off, 0);
                    }
                    etPassword.setSelection(etPassword.getText().length());
                    return true;
                }
            }
            return false;
        });

        // Đăng nhập bằng email + password
        btnLogin.setOnClickListener(v -> {
            android.util.Log.d("LoginActivity", "Login button clicked");

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            android.util.Log.d("LoginActivity", "Email: " + email + ", Password length: " + password.length());

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }

            android.util.Log.d("LoginActivity", "About to call userRepository.login");

            try {
                userRepository.login(email, password, user -> {
                    android.util.Log.d("LoginActivity", "Login callback received, user: " + (user != null ? user.getName() : "null"));

                    if (this.isFinishing() || this.isDestroyed()) {
                        android.util.Log.d("LoginActivity", "Activity is finishing, skipping UI update");
                        return; // Activity is no longer valid
                    }

                    runOnUiThread(() -> {
                        try {
                            if (user != null) {
                                android.util.Log.d("LoginActivity", "Login successful, saving state");
                                // Lưu trạng thái đăng nhập
                                saveLoginState(user, email);
                                Toast.makeText(LoginActivity.this, "Hello " + user.getName(), Toast.LENGTH_SHORT).show();
                                navigateAfterLogin();
                            } else {
                                android.util.Log.d("LoginActivity", "Login failed - invalid credentials");
                                Toast.makeText(LoginActivity.this, "Email hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            android.util.Log.e("LoginActivity", "UI update error: " + e.getMessage(), e);
                            Toast.makeText(LoginActivity.this, "Đã xảy ra lỗi, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

                android.util.Log.d("LoginActivity", "userRepository.login called successfully");

            } catch (Exception e) {
                android.util.Log.e("LoginActivity", "Login error: " + e.getMessage(), e);
                Toast.makeText(this, "Đã xảy ra lỗi, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        });

        // Đăng ký
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Đăng nhập bằng Google
        btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    private void saveLoginState(UserEntity user, String email) {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.putString(KEY_EMAIL, email);
        editor.putInt(KEY_ID, user.getUserId());
        editor.putString(KEY_NAME, user.getName());
        editor.apply();
    }

    // Xử lý kết quả đăng nhập bằng Google
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            var task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    handleGoogleSignIn(account);
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Đăng nhập bằng Google thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleGoogleSignIn(GoogleSignInAccount account) {
        Toast.makeText(this, "Đăng nhập bằng Google thành công: " + account.getEmail(), Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                UserEntity user = db.userDAO().getUserByEmail(account.getEmail());

                runOnUiThread(() -> {
                    // Lưu trạng thái đăng nhập bằng Google
                    SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(KEY_LOGGED_IN, true);
                    editor.putString(KEY_EMAIL, account.getEmail());
                    editor.putString(KEY_NAME, account.getDisplayName());

                    if (user != null) {
                        editor.putInt(KEY_ID, user.getUserId());
                    }
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Hello " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
                    navigateAfterLogin();
                });

                if (user != null) {
                    // User exists, add auth provider
                    AuthProviderEntity authEntity = new AuthProviderEntity(
                            0,
                            user.getUserId(),
                            "GOOGLE",
                            account.getId()
                    );
                    db.authProviderDAO().insertAuthProvider(authEntity);
                } else {
                    // New user, register them
                    userRepository.register(
                            account.getDisplayName(),
                            25,
                            true,
                            account.getEmail(),
                            "123456");
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Lỗi xử lý đăng nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void navigateAfterLogin() {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean surveyCompleted = pref.getBoolean(KEY_SURVEY_COMPLETED, false);

        Intent intent;
        if (surveyCompleted) {
            // User has completed survey, go to main app
            intent = new Intent(LoginActivity.this, MainActivity.class);
        } else {
            // First time login, go to survey
            intent = new Intent(LoginActivity.this, UserSurveyActivity.class);
        }

        startActivity(intent);
        finish();
    }

    // Kiểm tra đăng nhập tự động
    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isLoggedIn = pref.getBoolean(KEY_LOGGED_IN, false);

        // Kiểm tra đăng nhập bằng SharedPreferences
        if (isLoggedIn) {
            navigateAfterLogin();
            return;
        }

        // Kiểm tra đăng nhập bằng tài khoản Google
        var account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // Save Google account info and navigate
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(KEY_LOGGED_IN, true);
            editor.putString(KEY_EMAIL, account.getEmail());
            editor.putString(KEY_NAME, account.getDisplayName());
            editor.apply();

            navigateAfterLogin();
        }
    }
}
