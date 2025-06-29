package com.example.fitnestx.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.fitnestx.data.model.WorkoutPlan;

@Entity(tableName = "WORKOUT_PLAN")
public class WorkoutPlanEntity implements WorkoutPlan {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int planId;
    private int userId;
    private String startDate;
    private String durationInWeeks;
    private int daysPerWeek;
    private boolean isActive;

    public WorkoutPlanEntity(int planId, int userId, String startDate, String durationInWeeks, int daysPerWeek, boolean isActive) {
        this.planId = planId;
        this.userId = userId;
        this.startDate = startDate;
        this.durationInWeeks = durationInWeeks;
        this.daysPerWeek = daysPerWeek;
        this.isActive = isActive;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setDurationInWeeks(String durationInWeeks) {
        this.durationInWeeks = durationInWeeks;
    }

    public void setDaysPerWeek(int daysPerWeek) {
        this.daysPerWeek = daysPerWeek;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public int getPlanId() {
        return planId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getStartDate() {
        return startDate;
    }

    @Override
    public String getDurationInWeeks() {
        return durationInWeeks;
    }

    @Override
    public int getDaysPerWeek() {
        return daysPerWeek;
    }

    @Override
    public boolean getIsActive() {
        return isActive;
    }
}
