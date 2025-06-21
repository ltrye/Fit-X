package com.example.fitnestx.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.fitnestx.data.model.SessionExercise;

@Entity(tableName = "SESSION_EXERCISE", primaryKeys = {"sessionId", "exerciseId"})
public class SessionExerciseEntity implements SessionExercise {

    @NonNull
    private int sessionId;
    @NonNull
    private int exerciseId;
    private int order;
    private String restTime;
    private int sets;
    private int reps;
    private boolean isOptional;

    public SessionExerciseEntity(int sessionId, int exerciseId, int order, String restTime, int sets, int reps, boolean isOptional) {
        this.sessionId = sessionId;
        this.exerciseId = exerciseId;
        this.order = order;
        this.restTime = restTime;
        this.sets = sets;
        this.reps = reps;
        this.isOptional = isOptional;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setRestTime(String restTime) {
        this.restTime = restTime;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public void setOptional(boolean optional) {
        isOptional = optional;
    }

    @Override
    public int getSessionId() {
        return sessionId;
    }

    @Override
    public int getExerciseId() {
        return exerciseId;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public String getRestTime() {
        return restTime;
    }

    @Override
    public int getSets() {
        return sets;
    }

    @Override
    public int getReps() {
        return reps;
    }

    @Override
    public boolean getIsOptional() {
        return isOptional;
    }
}
