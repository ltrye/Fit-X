package com.example.fitnestx.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import com.example.fitnestx.data.AppDatabase;
import com.example.fitnestx.data.dao.NotificationDAO;
import com.example.fitnestx.data.entity.NotificationEntity;

public class NotificationRepository {

    private final NotificationDAO notificationDAO;

    public NotificationRepository(Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        notificationDAO = appDatabase.notificationDAO();
    }

    public void insertNotification(NotificationEntity notification) {
        new Thread(() -> notificationDAO.insertNotification(notification)).start();
    }

    public LiveData<List<NotificationEntity>> getAllNotifications() {
        return notificationDAO.getAllNotifications();
    }

    public NotificationEntity getNotificationById(int notificationId) {
        return notificationDAO.getNotificationById(notificationId);
    }

    public LiveData<List<NotificationEntity>> getNotificationsByUserId(int userId) {
        return notificationDAO.getNotificationsByUserId(userId);
    }

    public void updateNotification(NotificationEntity notification) {
        new Thread(() -> notificationDAO.updateNotification(notification)).start();
    }

    public void deleteNotification(NotificationEntity notification) {
        new Thread(() -> notificationDAO.deleteNotification(notification)).start();
    }

    public void deleteAllNotifications() {
        new Thread(() -> notificationDAO.deleteAllNotifications()).start();
    }
}
