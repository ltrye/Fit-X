package com.example.fitnestx.viewmodel;

import com.example.fitnestx.data.entity.ExerciseEntity;

public class ExerciseWithSessionStatus {
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
