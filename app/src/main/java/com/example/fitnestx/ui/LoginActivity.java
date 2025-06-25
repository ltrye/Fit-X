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

    // 👇 Tên file SharedPreferences
    private static final String PREF_NAME = "MyAppPrefs";
    private static final String KEY_LOGGED_IN = "isLoggedIn";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_ID = "user_id";
    private static final String KEY_NAME = "user_name";

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
//                .requestIdToken(getString(R.string.default_web_client_id))
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

            userRepository.login(email, password, user -> {
                runOnUiThread(() -> {
                    if (user != null) {
                        // Lưu trạng thái đăng nhập
                        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean(KEY_LOGGED_IN, true);
                        editor.putString(KEY_EMAIL, email);
                        editor.putInt(KEY_ID, user.getUserId());
                        editor.putString(KEY_NAME, user.getName());
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "Hello " + user.getName(), Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Email hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
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

    // Xử lý kết quả đăng nhập bằng Google
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            var task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    Toast.makeText(this, "Đăng nhập bằng Google thành công: " + account.getEmail(), Toast.LENGTH_SHORT).show();

                    // Lưu trạng thái đăng nhập bằng Google
                    SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(KEY_LOGGED_IN, true);
                    editor.putString(KEY_EMAIL, account.getEmail());
                    editor.putString(KEY_NAME, account.getDisplayName());
                    editor.apply();

                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

                        UserEntity user = db.userDAO().getUserByEmail(account.getEmail());

                        if (user != null) {
                            AuthProviderEntity authEntity = new AuthProviderEntity(
                                    0,
                                    user.getUserId(),
                                    "GOOGLE",
                                    account.getId()
                            );

                            db.authProviderDAO().insertAuthProvider(authEntity);
                        } else {
                            userRepository.register(
                                    account.getDisplayName(),
                                    25,
                                    true,
                                    account.getEmail(),
                                    "123456");
                        }
                    }).start();

                    Toast.makeText(LoginActivity.this, "Hello " + account.getDisplayName(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Đăng nhập bằng Google thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Kiểm tra đăng nhập tự động
    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isLoggedIn = pref.getBoolean(KEY_LOGGED_IN, false);

        // Kiểm tra đăng nhập bằng SharedPreferences
        if (isLoggedIn) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Kiểm tra đăng nhập bằng tài khoản Google
        var account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
