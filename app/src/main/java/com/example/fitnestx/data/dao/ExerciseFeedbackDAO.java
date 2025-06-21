package com.example.fitnestx.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fitnestx.data.entity.ExerciseFeedbackEntity;

import java.util.List;

@Dao
public interface ExerciseFeedbackDAO {
    @Query("SELECT * FROM EXERCISE_FEEDBACK")
    LiveData<List<ExerciseFeedbackEntity>> getAllFeedback();

    @Query("SELECT * FROM EXERCISE_FEEDBACK WHERE feedbackId = :feedbackId")
    ExerciseFeedbackEntity getFeedbackById(int feedbackId);

    @Query("SELECT * FROM EXERCISE_FEEDBACK WHERE userId = :userId")
    LiveData<List<ExerciseFeedbackEntity>> getFeedbackByUserId(int userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFeedback(ExerciseFeedbackEntity feedback);

    @Update
    void updateFeedback(ExerciseFeedbackEntity feedback);

    @Delete
    void deleteFeedback(ExerciseFeedbackEntity feedback);

    @Query("DELETE FROM EXERCISE_FEEDBACK")
    void deleteAllFeedback();
}
