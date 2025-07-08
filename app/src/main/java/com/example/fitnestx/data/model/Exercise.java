package com.example.fitnestx.data.model;

import java.io.Serializable;

public interface Exercise extends Serializable {
    public int getExerciseId();
    public String getName();
    public String getDescription();
    public String getVideoURL();
    public int getMuscleGroupId();
    public boolean getEquipmentRequired();
    public int getDifficulty();
}
