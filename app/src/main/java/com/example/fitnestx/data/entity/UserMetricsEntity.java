package com.example.fitnestx.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.example.fitnestx.data.model.UserMetrics;

@Entity(tableName = "USER_METRICS",
        foreignKeys = @ForeignKey(entity = UserEntity.class, parentColumns = "id", childColumns = "userId"))
public class UserMetricsEntity implements UserMetrics {

    @PrimaryKey
    @NonNull
    private int metricId;
    private int userId;
    private String timestamp;
    private double weight;
    private double height;
    private double bmi;
    private String goal;

    public UserMetricsEntity(int metricId, int userId, String timestamp, double weight, double height, double bmi, String goal) {
        this.metricId = metricId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.weight = weight;
        this.height = height;
        this.bmi = bmi;
        this.goal = goal;
    }

    public void setMetricId(int metricId) {
        this.metricId = metricId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setBmi(double bmi) {
        this.bmi = bmi;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    @Override
    public int getMetricId() {
        return metricId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public double getBmi() {
        return bmi;
    }

    @Override
    public String getGoal() {
        return goal;
    }
}
