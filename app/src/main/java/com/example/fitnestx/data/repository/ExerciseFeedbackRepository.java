package com.example.fitnestx.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import com.example.fitnestx.data.AppDatabase;
import com.example.fitnestx.data.dao.ExerciseFeedbackDAO;
import com.example.fitnestx.data.entity.ExerciseFeedbackEntity;

public class ExerciseFeedbackRepository {

    private final ExerciseFeedbackDAO exerciseFeedbackDAO;

    public ExerciseFeedbackRepository(Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        exerciseFeedbackDAO = appDatabase.exerciseFeedbackDAO();
    }

    public void insertFeedback(ExerciseFeedbackEntity feedback) {
        new Thread(() -> exerciseFeedbackDAO.insertFeedback(feedback)).start();
    }

    public LiveData<List<ExerciseFeedbackEntity>> getAllFeedback() {
        return exerciseFeedbackDAO.getAllFeedback();
    }

    public ExerciseFeedbackEntity getFeedbackById(int feedbackId) {
        return exerciseFeedbackDAO.getFeedbackById(feedbackId);
    }

    public LiveData<List<ExerciseFeedbackEntity>> getFeedbackByUserId(int userId) {
        return exerciseFeedbackDAO.getFeedbackByUserId(userId);
    }

    public void updateFeedback(ExerciseFeedbackEntity feedback) {
        new Thread(() -> exerciseFeedbackDAO.updateFeedback(feedback)).start();
    }

    public void deleteFeedback(ExerciseFeedbackEntity feedback) {
        new Thread(() -> exerciseFeedbackDAO.deleteFeedback(feedback)).start();
    }

    public void deleteAllFeedback() {
        new Thread(() -> exerciseFeedbackDAO.deleteAllFeedback()).start();
    }
}
