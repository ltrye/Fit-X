package com.example.fitnestx.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.example.fitnestx.data.model.Notification;

@Entity(tableName = "NOTIFICATION",
        foreignKeys = @ForeignKey(entity = UserEntity.class, parentColumns = "userId", childColumns = "userId"))
public class NotificationEntity implements Notification {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int notificationId;
    private int userId;
    private String content;
    private boolean isRead;
    private String timestamp;

    public NotificationEntity(int notificationId, int userId, String content, boolean isRead, String timestamp) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.content = content;
        this.isRead = isRead;
        this.timestamp = timestamp;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int getNotificationId() {
        return notificationId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public boolean getIsRead() {
        return isRead;
    }

    @Override
    public String getTimestamp() {
        return timestamp;
    }
}
