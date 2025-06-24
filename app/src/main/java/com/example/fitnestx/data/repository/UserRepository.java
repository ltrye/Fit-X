package com.example.fitnestx.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import com.example.fitnestx.data.AppDatabase;
import com.example.fitnestx.data.dao.UserDAO;
import com.example.fitnestx.data.entity.UserEntity;
import com.example.fitnestx.utils.PasswordUtils;

public class UserRepository {

    private final UserDAO userDAO;

    public UserRepository(Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        userDAO = appDatabase.userDAO();
    }

    public void insertUser(UserEntity user) {
        new Thread(() -> userDAO.insertUser(user)).start();
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
        new Thread(() -> userDAO.updateUser(userEntity)).start();
    }

    public void deleteUser(UserEntity user) {
        new Thread(() -> userDAO.deleteUser(user)).start();
    }

    public void deleteAllUsers() {
        new Thread(() -> userDAO.deleteAllUsers()).start();
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
            UserEntity user = userDAO.getUserByEmail(email);
            if (user != null && PasswordUtils.verifyPassword(plainPassword, user.getPassword())) {
                callback.onResult(user);
            } else {
                callback.onResult(null);
            }
        }).start();
    }

    public void register(String name, int age, boolean gender, String email, String plainPassword) {
        String hashedPassword = PasswordUtils.hashPassword(plainPassword);
        UserEntity user = new UserEntity(
                (int) System.currentTimeMillis(),
                name,
                age,
                gender,
                email,
                hashedPassword,
                true
        );
        insertUser(user);
    }

    public interface LoginCallback {
        void onResult(UserEntity user);
    }

    public interface CheckExistCallback {
        void onResult(UserEntity user);
    }
}

