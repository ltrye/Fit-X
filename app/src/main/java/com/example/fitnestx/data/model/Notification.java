package com.example.fitnestx.data.model;

public interface Notification {
    public int getNotificationId();
    public int getUserId();
    public String getContent();
    public boolean getIsRead();
    public String getTimestamp();
}
