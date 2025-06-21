package com.example.fitnestx.data.model;

public interface ExerciseFeedback {
    public int getFeedbackId();
    public int getUserId();
    public int getExerciseId();
    public int getDifficultyRated();
    public String getComment();
    public String getTimestamp();
}
