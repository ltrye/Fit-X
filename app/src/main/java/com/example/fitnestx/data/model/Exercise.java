package com.example.fitnestx.data.model;

public interface Exercise {
    public int getExerciseId();
    public String getName();
    public String getDescription();
    public String getVideoURL();
    public int getMuscleGroupId();
    public boolean getEquipmentRequired();
    public int getDifficulty();
}
