package com.example.fitnestx.data.model;

public interface WorkoutSession {
    public int getSessionId();
    public int getPlanId();
    public String getDate();
    public int getIntensity();
    public boolean getIsCompleted();
}
