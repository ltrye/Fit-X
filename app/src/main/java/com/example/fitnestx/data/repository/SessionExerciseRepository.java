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

    public void insertSessionExercise(SessionExerciseEntity sessionExercise) {
        new Thread(() -> sessionExerciseDAO.insertSessionExercise(sessionExercise)).start();
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
        new Thread(() -> sessionExerciseDAO.updateSessionExercise(sessionExercise)).start();
    }

    public void deleteSessionExercise(SessionExerciseEntity sessionExercise) {
        new Thread(() -> sessionExerciseDAO.deleteSessionExercise(sessionExercise)).start();
    }

    public void deleteAllSessionExercises() {
        new Thread(() -> sessionExerciseDAO.deleteAllSessionExercises()).start();
    }
}
