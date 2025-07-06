package com.example.fitnestx.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }


            try {
                userRepository.login(email, password, user -> {
                    if (isFinishing() || isDestroyed()) {
                        Log.d("LoginActivity", "Activity is finishing, skipping UI update");
                        return;

                    }

                    runOnUiThread(() -> {
                        try {
                            if (user != null) {
                                saveLoginState(user, email);
                                Toast.makeText(LoginActivity.this, "Hello " + user.getName(), Toast.LENGTH_SHORT).show();
                                navigateAfterLogin();
                            } else {
                                Toast.makeText(LoginActivity.this, "Email hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("LoginActivity", "UI update error: " + e.getMessage(), e);
                            Toast.makeText(LoginActivity.this, "Đã xảy ra lỗi, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            } catch (Exception e) {
                Log.e("LoginActivity", "Login error: " + e.getMessage(), e);
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

        // Also save userId to AuthPrefs for consistency
        SharedPreferences authPrefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        authPrefs.edit().putInt("userId", user.getUserId()).apply();
        Log.d("LoginActivity", "Saved userId to AuthPrefs: " + user.getUserId());
    }

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
                Log.e("LoginActivity", "Google Sign-In failed: " + e.getMessage(), e);
                Toast.makeText(this, "Đăng nhập bằng Google thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleGoogleSignIn(GoogleSignInAccount account) {
        Log.d("LoginActivity", "Google Sign-In successful: " + account.getEmail());
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                UserEntity user = db.userDAO().getUserByEmail(account.getEmail());

                if (user == null) {
                    // New user, register them
                    userRepository.register(
                            account.getDisplayName(),
                            25, // Default age
                            true, // Default gender
                            account.getEmail(),
                            "google_default_password" // Placeholder password
                    );
                    // Wait briefly for registration to complete
                    Thread.sleep(100);
                    user = db.userDAO().getUserByEmail(account.getEmail());
                }

                if (user != null) {
                    // Add auth provider
                    AuthProviderEntity authEntity = new AuthProviderEntity(
                            0,
                            user.getUserId(),
                            "GOOGLE",
                            account.getId()
                    );
                    db.authProviderDAO().insertAuthProvider(authEntity);

                    // Save login state
                    UserEntity finalUser = user;
                    runOnUiThread(() -> {
                        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean(KEY_LOGGED_IN, true);
                        editor.putString(KEY_EMAIL, account.getEmail());
                        editor.putString(KEY_NAME, account.getDisplayName());
                        editor.putInt(KEY_ID, finalUser.getUserId());
                        editor.apply();

                        // Also save to AuthPrefs
                        SharedPreferences authPrefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
                        authPrefs.edit().putInt("userId", finalUser.getUserId()).apply();
                        Log.d("LoginActivity", "Saved userId to AuthPrefs (Google): " + finalUser.getUserId());

                        Toast.makeText(LoginActivity.this, "Hello " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
                        navigateAfterLogin();
                    });
                } else {
                    runOnUiThread(() -> {
                        Log.e("LoginActivity", "Failed to retrieve or register user for email: " + account.getEmail());
                        Toast.makeText(LoginActivity.this, "Lỗi đăng ký người dùng Google", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e("LoginActivity", "Google Sign-In error: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Lỗi xử lý đăng nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void navigateAfterLogin() {
        Intent intent = new Intent(LoginActivity.this, PlanActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isLoggedIn = pref.getBoolean(KEY_LOGGED_IN, false);

        if (isLoggedIn) {
            navigateAfterLogin();
            return;
        }

        var account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(KEY_LOGGED_IN, true);
            editor.putString(KEY_EMAIL, account.getEmail());
            editor.putString(KEY_NAME, account.getDisplayName());
            editor.apply();

            // Save userId to AuthPrefs if user exists
            new Thread(() -> {
                UserEntity user = userRepository.getUserById(userRepository.getIdByEmail(account.getEmail()));
                if (user != null) {
                    SharedPreferences.Editor prefEditor = pref.edit();
                    prefEditor.putBoolean(KEY_LOGGED_IN, true);
                    prefEditor.putString(KEY_EMAIL, account.getEmail());
                    prefEditor.putString(KEY_NAME, account.getDisplayName());
                    prefEditor.putInt(KEY_ID, user.getUserId());
                    prefEditor.apply();

                    SharedPreferences authPrefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
                    authPrefs.edit().putInt("userId", user.getUserId()).apply();
                    Log.d("LoginActivity", "Saved userId to AuthPrefs (auto-login): " + user.getUserId());
                }
                runOnUiThread(this::navigateAfterLogin);
            }).start();
        }
    }
}
