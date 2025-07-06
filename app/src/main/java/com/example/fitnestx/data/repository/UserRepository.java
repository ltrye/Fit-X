package com.example.fitnestx.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.fitnestx.data.AppDatabase;
import com.example.fitnestx.data.dao.UserDAO;
import com.example.fitnestx.data.entity.UserEntity;
import com.example.fitnestx.utils.PasswordUtils;

import java.util.List;

public class UserRepository {

    private final UserDAO userDAO;
    private final Context context;

    public UserRepository(Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        userDAO = appDatabase.userDAO();
        this.context = context;
    }

    public void insertUser(UserEntity user) {
        new Thread(() -> {
            try {
                userDAO.insertUser(user);
                Log.d("UserRepository", "Successfully inserted user with ID: " + user.getUserId());
            } catch (Exception e) {
                Log.e("UserRepository", "Failed to insert user", e);
            }
        }).start();
    }

    public LiveData<List<UserEntity>> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public UserEntity getUserById(int userId) {
        return userDAO.getUserById(userId);
    }

    public int getIdByEmail(String email) {
        return userDAO.getIdByEmail(email);
    }

    public void updateUser(UserEntity userEntity) {
        new Thread(() -> {
            try {
                userDAO.updateUser(userEntity);
                Log.d("UserRepository", "Successfully updated user with ID: " + userEntity.getUserId());
            } catch (Exception e) {
                Log.e("UserRepository", "Failed to update user", e);
            }
        }).start();
    }

    public void deleteUser(UserEntity user) {
        new Thread(() -> {
            try {
                userDAO.deleteUser(user);
                Log.d("UserRepository", "Successfully deleted user with ID: " + user.getUserId());
            } catch (Exception e) {
                Log.e("UserRepository", "Failed to delete user", e);
            }
        }).start();
    }

    public void deleteAllUsers() {
        new Thread(() -> {
            try {
                userDAO.deleteAllUsers();
                Log.d("UserRepository", "Successfully deleted all users");
            } catch (Exception e) {
                Log.e("UserRepository", "Failed to delete all users", e);
            }
        }).start();
    }

    public LiveData<List<UserEntity>> getActiveUsers() {
        return userDAO.getActiveUsers();
    }

    public void checkExistAsync(String email, CheckExistCallback callback) {
        new Thread(() -> {
            UserEntity user = userDAO.getUserByEmail(email);
            callback.onResult(user);
        }).start();
    }

    public void login(String email, String plainPassword, LoginCallback callback) {
        new Thread(() -> {
            try {
                UserEntity user = userDAO.getUserByEmail(email);
                if (user != null && PasswordUtils.verifyPassword(plainPassword, user.getPassword())) {
                    // Save userId to SharedPreferences
                    SharedPreferences prefs = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
                    prefs.edit().putInt("userId", user.getUserId()).apply();
                    Log.d("UserRepository", "Login successful, saved userId: " + user.getUserId());
                    callback.onResult(user);
                } else {
                    Log.w("UserRepository", "Login failed for email: " + email);
                    callback.onResult(null);
                }
            } catch (Exception e) {
                Log.e("UserRepository", "Login failed", e);
                callback.onResult(null);
            }
        }).start();
    }

    public void register(String name, int age, boolean gender, String email, String plainPassword) {
        String hashedPassword = PasswordUtils.hashPassword(plainPassword);
        UserEntity user = new UserEntity(
                0, // Let Room auto-generate userId
                name,
                age,
                gender,
                email,
                hashedPassword,
                true
        );
        new Thread(() -> {
            try {
                userDAO.insertUser(user);
                // Retrieve the inserted user to get the auto-generated userId
                UserEntity insertedUser = userDAO.getUserByEmail(email);
                if (insertedUser != null) {
                    SharedPreferences prefs = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
                    prefs.edit().putInt("userId", insertedUser.getUserId()).apply();
                    Log.d("UserRepository", "Successfully registered user with ID: " + insertedUser.getUserId());
                } else {
                    Log.e("UserRepository", "Failed to retrieve inserted user for email: " + email);
                }
            } catch (Exception e) {
                Log.e("UserRepository", "Registration failed", e);
            }
        }).start();
    }

    public interface LoginCallback {
        void onResult(UserEntity user);
    }

    public interface CheckExistCallback {
        void onResult(UserEntity user);
    }
}
