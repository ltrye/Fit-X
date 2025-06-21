package com.example.fitnestx.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import com.example.fitnestx.data.AppDatabase;
import com.example.fitnestx.data.dao.UserMetricsDAO;
import com.example.fitnestx.data.entity.UserMetricsEntity;

public class UserMetricsRepository {

    private final UserMetricsDAO userMetricsDAO;

    public UserMetricsRepository(Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        userMetricsDAO = appDatabase.userMetricsDAO();
    }

    public void insertUserMetric(UserMetricsEntity metric) {
        new Thread(() -> userMetricsDAO.insertUserMetric(metric)).start();
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
        new Thread(() -> userMetricsDAO.updateUserMetric(metric)).start();
    }

    public void deleteUserMetric(UserMetricsEntity metric) {
        new Thread(() -> userMetricsDAO.deleteUserMetric(metric)).start();
    }

    public void deleteAllUserMetrics() {
        new Thread(() -> userMetricsDAO.deleteAllUserMetrics()).start();
    }
}
