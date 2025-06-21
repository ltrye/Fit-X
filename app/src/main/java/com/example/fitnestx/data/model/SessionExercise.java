package com.example.fitnestx.data.model;

public interface SessionExercise {
    public int getSessionId();
    public int getExerciseId();
    public int getOrder();
    public String getRestTime();
    public int getSets();
    public int getReps();
    public boolean getIsOptional();
}
