package com.example.fitnestx.Helpers;

import com.example.fitnestx.data.entity.ExerciseEntity;

public class SectionItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private int type;
    private String headerTitle;
    private ExerciseEntity exercise;

    // Constructor cho Header
    public SectionItem(String headerTitle) {
        this.type = TYPE_HEADER;
        this.headerTitle = headerTitle;
    }

    // Constructor cho Item
    public SectionItem(ExerciseEntity exercise) {
        this.type = TYPE_ITEM;
        this.exercise = exercise;
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
