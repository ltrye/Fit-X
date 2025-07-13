package com.example.fitnestx.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import com.example.fitnestx.data.AppDatabase;
import com.example.fitnestx.data.dao.SessionExerciseDAO;
import com.example.fitnestx.data.entity.SessionExerciseEntity;

public class SessionExerciseRepository {

    private final SessionExerciseDAO sessionExerciseDAO;

    public SessionExerciseRepository(Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        sessionExerciseDAO = appDatabase.sessionExerciseDAO();
    }
    public List<Integer> GetListIdExercsieBySessionId(int sessionId) {
        return sessionExerciseDAO.GetListIdExercsieBySessionId(sessionId);
    }
    public List<SessionExerciseEntity> getExercisesListBySessionId(int sessionId) {
        return sessionExerciseDAO.getExercisesListBySessionId(sessionId);
    }
    public int TotalSessionExerciseById(int sessionId) {
        return sessionExerciseDAO.TotalSessionExercise(sessionId);
    }
    public void insertSessionExercise(SessionExerciseEntity sessionExercise) {
        sessionExerciseDAO.insertSessionExercise(sessionExercise);
    }

    public LiveData<List<SessionExerciseEntity>> getAllSessionExercises() {
        return sessionExerciseDAO.getAllSessionExercises();
    }

    public SessionExerciseEntity getSessionExercise(int sessionId, int exerciseId) {
        return sessionExerciseDAO.getSessionExercise(sessionId, exerciseId);
    }

    public LiveData<List<SessionExerciseEntity>> getExercisesBySessionId(int sessionId) {
        return sessionExerciseDAO.getExercisesBySessionId(sessionId);
    }

    public void updateSessionExercise(SessionExerciseEntity sessionExercise) {
        sessionExerciseDAO.updateSessionExercise(sessionExercise);
    }

    public void deleteSessionExercise(SessionExerciseEntity sessionExercise) {
        sessionExerciseDAO.deleteSessionExercise(sessionExercise);
    }

    public void deleteAllSessionExercises() {
        sessionExerciseDAO.deleteAllSessionExercises();
    }

    // New method to update all exercises' marked status for a given plan
    public void updateAllExercisesMarkedStatusForPlan(int planId, boolean isMarked) {
        sessionExerciseDAO.updateAllExercisesMarkedStatusForPlan(planId, isMarked);
    }

    // New method to delete all session exercises for a given plan
    public void deleteAllSessionExercisesForPlan(int planId) {
        sessionExerciseDAO.deleteAllSessionExercisesForPlan(planId);
    }

    // New method to delete all session exercises for a specific session
    public void deleteSessionExercisesBySessionId(int sessionId) {
        sessionExerciseDAO.deleteSessionExercisesBySessionId(sessionId);
    }
}
