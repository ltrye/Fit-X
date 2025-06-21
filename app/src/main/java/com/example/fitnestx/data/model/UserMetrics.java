package com.example.fitnestx.data.model;

public interface UserMetrics {
    public int getMetricId();
    public int getUserId();
    public String getTimestamp();
    public double getWeight();
    public double getHeight();
    public double getBmi();
    public String getGoal();
}
