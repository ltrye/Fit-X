package com.example.fitnestx.viewmodel;

import com.example.fitnestx.data.entity.ExerciseEntity;

import java.io.Serializable;

public class ExerciseWithSessionStatus implements Serializable {
    private ExerciseEntity exercise;
    private boolean isMarked; // lấy từ SessionExerciseEntity

    public ExerciseWithSessionStatus(ExerciseEntity exercise, boolean isMarked) {
        this.exercise = exercise;
        this.isMarked = isMarked;
    }

    public ExerciseEntity getExercise() {
        return exercise;
    }

    public boolean isMarked() {
        return isMarked;
    }

    public void setMarked(boolean marked) {
        this.isMarked = marked;
    }
}
