package com.example.fitnestx.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.example.fitnestx.data.model.ExerciseFeedback;

@Entity(tableName = "EXERCISE_FEEDBACK",
        foreignKeys = {
                @ForeignKey(entity = UserEntity.class, parentColumns = "id", childColumns = "userId"),
                @ForeignKey(entity = ExerciseEntity.class, parentColumns = "exerciseId", childColumns = "exerciseId")
        })
public class ExerciseFeedbackEntity implements ExerciseFeedback {

    @PrimaryKey
    @NonNull
    private int feedbackId;
    private int userId;
    private int exerciseId;
    private int difficultyRated;
    private String comment;
    private String timestamp;

    public ExerciseFeedbackEntity(int feedbackId, int userId, int exerciseId, int difficultyRated, String comment, String timestamp) {
        this.feedbackId = feedbackId;
        this.userId = userId;
        this.exerciseId = exerciseId;
        this.difficultyRated = difficultyRated;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public void setFeedbackId(int feedbackId) {
        this.feedbackId = feedbackId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public void setDifficultyRated(int difficultyRated) {
        this.difficultyRated = difficultyRated;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int getFeedbackId() {
        return feedbackId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public int getExerciseId() {
        return exerciseId;
    }

    @Override
    public int getDifficultyRated() {
        return difficultyRated;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public String getTimestamp() {
        return timestamp;
    }
}
