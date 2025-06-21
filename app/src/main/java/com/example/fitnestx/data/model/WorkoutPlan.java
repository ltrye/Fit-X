package com.example.fitnestx.data.model;

public interface WorkoutPlan {
    public int getPlanId();
    public int getUserId();
    public String getStartDate();
    public String getDurationInWeeks();
    public int getDaysPerWeek();
    public boolean getIsActive();
}
