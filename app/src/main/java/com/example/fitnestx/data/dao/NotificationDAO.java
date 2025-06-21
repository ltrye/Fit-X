package com.example.fitnestx.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fitnestx.data.entity.NotificationEntity;

import java.util.List;

@Dao
public interface NotificationDAO {
    @Query("SELECT * FROM NOTIFICATION")
    LiveData<List<NotificationEntity>> getAllNotifications();

    @Query("SELECT * FROM NOTIFICATION WHERE notificationId = :notificationId")
    NotificationEntity getNotificationById(int notificationId);

    @Query("SELECT * FROM NOTIFICATION WHERE userId = :userId")
    LiveData<List<NotificationEntity>> getNotificationsByUserId(int userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNotification(NotificationEntity notification);

    @Update
    void updateNotification(NotificationEntity notification);

    @Delete
    void deleteNotification(NotificationEntity notification);

    @Query("DELETE FROM NOTIFICATION")
    void deleteAllNotifications();
}
