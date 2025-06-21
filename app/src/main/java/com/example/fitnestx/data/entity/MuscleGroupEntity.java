package com.example.fitnestx.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.fitnestx.data.model.MuscleGroup;

@Entity(tableName = "MUSCLE_GROUP")
public class MuscleGroupEntity implements MuscleGroup {

    @PrimaryKey
    @NonNull
    private int muscleGroupId;
    private String name;
    private double recoveryTimeInHours;

    public MuscleGroupEntity(int muscleGroupId, String name, double recoveryTimeInHours) {
        this.muscleGroupId = muscleGroupId;
        this.name = name;
        this.recoveryTimeInHours = recoveryTimeInHours;
    }

    public void setMuscleGroupId(int muscleGroupId) {
        this.muscleGroupId = muscleGroupId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRecoveryTimeInHours(double recoveryTimeInHours) {
        this.recoveryTimeInHours = recoveryTimeInHours;
    }

    @Override
    public int getMuscleGroupId() {
        return muscleGroupId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getRecoveryTimeInHours() {
        return recoveryTimeInHours;
    }
}
