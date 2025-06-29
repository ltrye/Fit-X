package com.example.fitnestx.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.example.fitnestx.data.model.WorkoutSession;

@Entity(tableName = "WORKOUT_SESSION",
        foreignKeys = @ForeignKey(entity = WorkoutPlanEntity.class, parentColumns = "planId", childColumns = "planId"))
public class WorkoutSessionEntity implements WorkoutSession {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int sessionId;
    private int planId;
    private String date;
    private int intensity;
    private boolean isCompleted;

    public WorkoutSessionEntity(int sessionId, int planId, String date, int intensity, boolean isCompleted) {
        this.sessionId = sessionId;
        this.planId = planId;
        this.date = date;
        this.intensity = intensity;
        this.isCompleted = isCompleted;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    @Override
    public int getSessionId() {
        return sessionId;
    }

    @Override
    public int getPlanId() {
        return planId;
    }

    @Override
    public String getDate() {
        return date;
    }

    @Override
    public int getIntensity() {
        return intensity;
    }

    @Override
    public boolean getIsCompleted() {
        return isCompleted;
    }
}
