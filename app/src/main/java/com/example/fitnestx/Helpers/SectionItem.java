package com.example.fitnestx.Helpers;

import com.example.fitnestx.data.entity.ExerciseEntity;
import com.example.fitnestx.viewmodel.ExerciseWithSessionStatus;

public class SectionItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;
    private ExerciseWithSessionStatus exerciseWithStatus;
    private int type;
    private String headerTitle;
    private ExerciseEntity exercise;
    public ExerciseWithSessionStatus getExerciseWithStatus() {
        return exerciseWithStatus;
    }

    public void setExerciseWithStatus(ExerciseWithSessionStatus e) {
        this.exerciseWithStatus = e;
    }

    // Constructor cho Header
    public SectionItem(String headerTitle) {
        this.type = TYPE_HEADER;
        this.headerTitle = headerTitle;
    }

    // Constructor cho Item
    public SectionItem(ExerciseWithSessionStatus exerciseWithStatus) {
        this.type = TYPE_ITEM;

        this.exerciseWithStatus = exerciseWithStatus;
    }

    public int getType() {
        return type;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public ExerciseEntity getExercise() {
        return exercise;
    }
}
