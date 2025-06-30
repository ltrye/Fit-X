package com.example.fitnestx.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.fitnestx.data.AppDatabase;
import com.example.fitnestx.data.dao.UserMetricsDAO;
import com.example.fitnestx.data.entity.UserMetricsEntity;

import java.util.List;

public class UserMetricsRepository {

    private final UserMetricsDAO userMetricsDAO;

    public UserMetricsRepository(Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        userMetricsDAO = appDatabase.userMetricsDAO();
    }

    public void insertUserMetric(UserMetricsEntity metric) {
        new Thread(() -> {
            try {
                userMetricsDAO.insertUserMetric(metric);
                Log.d("UserMetricsRepository", "Successfully inserted user metric for userId: " + metric.getUserId());
            } catch (Exception e) {
                Log.e("UserMetricsRepository", "Failed to insert user metric", e);
            }
        }).start();
    }

    public LiveData<List<UserMetricsEntity>> getAllUserMetrics() {
        return userMetricsDAO.getAllUserMetrics();
    }

    public UserMetricsEntity getUserMetricById(int metricId) {
        return userMetricsDAO.getUserMetricById(metricId);
    }

    public LiveData<List<UserMetricsEntity>> getUserMetricsByUserId(int userId) {
        return userMetricsDAO.getUserMetricsByUserId(userId);
    }

    public void updateUserMetric(UserMetricsEntity metric) {
        new Thread(() -> {
            try {
                userMetricsDAO.updateUserMetric(metric);
                Log.d("UserMetricsRepository", "Successfully updated user metric for userId: " + metric.getUserId());
            } catch (Exception e) {
                Log.e("UserMetricsRepository", "Failed to update user metric", e);
            }
        }).start();
    }

    public void deleteUserMetric(UserMetricsEntity metric) {
        new Thread(() -> {
            try {
                userMetricsDAO.deleteUserMetric(metric);
                Log.d("UserMetricsRepository", "Successfully deleted user metric for userId: " + metric.getUserId());
            } catch (Exception e) {
                Log.e("UserMetricsRepository", "Failed to delete user metric", e);
            }
        }).start();
    }

    public void deleteAllUserMetrics() {
        new Thread(() -> {
            try {
                userMetricsDAO.deleteAllUserMetrics();
                Log.d("UserMetricsRepository", "Successfully deleted all user metrics");
            } catch (Exception e) {
                Log.e("UserMetricsRepository", "Failed to delete all user metrics", e);
            }
        }).start();
    }
}
