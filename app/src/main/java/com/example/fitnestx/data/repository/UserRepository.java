package com.example.fitnestx.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import com.example.fitnestx.data.AppDatabase;
import com.example.fitnestx.data.dao.UserDAO;
import com.example.fitnestx.data.entity.UserEntity;

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
}
