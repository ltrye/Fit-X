package com.example.fitnestx.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.fitnestx.data.model.Exercise;

@Entity(tableName = "EXERCISE")
public class ExerciseEntity implements Exercise {

    @PrimaryKey
    @NonNull
    private int exerciseId;
    private String name;
    private String description;
    private String videoURL;
    private int muscleGroupId;
    private boolean equipmentRequired;
    private int difficulty;
    private String imageUrl;
    private boolean isMarked;

    public ExerciseEntity(int exerciseId, String name, String description, String videoURL,
                          int muscleGroupId, boolean equipmentRequired, int difficulty,
                          String imageUrl, boolean isMarked) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.description = description;
        this.videoURL = videoURL;
        this.muscleGroupId = muscleGroupId;
        this.equipmentRequired = equipmentRequired;
        this.difficulty = difficulty;
        this.imageUrl = imageUrl;
        this.isMarked = isMarked;
    }
    public boolean isMarked() {
        return isMarked;
    }

    public void setMarked(boolean marked) {
        isMarked = marked;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public void setMuscleGroupId(int muscleGroupId) {
        this.muscleGroupId = muscleGroupId;
    }

    public void setEquipmentRequired(boolean equipmentRequired) {
        this.equipmentRequired = equipmentRequired;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public int getExerciseId() {
        return exerciseId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getVideoURL() {
        return videoURL;
    }

    @Override
    public int getMuscleGroupId() {
        return muscleGroupId;
    }

    @Override
    public boolean getEquipmentRequired() {
        return equipmentRequired;
    }

    @Override
    public int getDifficulty() {
        return difficulty;
    }
}
