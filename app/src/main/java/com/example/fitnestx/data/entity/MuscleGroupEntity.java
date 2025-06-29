package com.example.fitnestx.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.fitnestx.data.model.MuscleGroup;


@Entity(
        tableName = "MUSCLE_GROUP",
        foreignKeys = @ForeignKey(
                entity = MuscleGroupEntity.class,
                parentColumns = "muscleGroupId",
                childColumns = "parentId",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = {@Index(value = "parentId")}
)
public class MuscleGroupEntity implements MuscleGroup {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int muscleGroupId;
    private String name;
    private double recoveryTimeInHours;
    @Nullable
    private Integer parentId;


    public MuscleGroupEntity(int muscleGroupId, String name, double recoveryTimeInHours, Integer parentId) {
        this.muscleGroupId = muscleGroupId;
        this.name = name;
        this.recoveryTimeInHours = recoveryTimeInHours;
        this.parentId = parentId;
    }

    public void setMuscleGroupId(int muscleGroupId) {
        this.muscleGroupId = muscleGroupId;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
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
