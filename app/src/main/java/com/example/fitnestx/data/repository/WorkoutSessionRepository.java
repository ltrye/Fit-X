package com.example.fitnestx.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import com.example.fitnestx.data.AppDatabase;
import com.example.fitnestx.data.dao.WorkoutSessionDAO;
import com.example.fitnestx.data.entity.WorkoutSessionEntity;

public class WorkoutSessionRepository {

    private final WorkoutSessionDAO workoutSessionDAO;

    public WorkoutSessionRepository(Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        workoutSessionDAO = appDatabase.workoutSessionDAO();
    }

    public void insertWorkoutSession(WorkoutSessionEntity session) {
        new Thread(() -> workoutSessionDAO.insertWorkoutSession(session)).start();
    }

    public LiveData<List<WorkoutSessionEntity>> getAllWorkoutSessions() {
        return workoutSessionDAO.getAllWorkoutSessions();
    }

    public WorkoutSessionEntity getWorkoutSessionById(int sessionId) {
        return workoutSessionDAO.getWorkoutSessionById(sessionId);
    }

    public LiveData<List<WorkoutSessionEntity>> getWorkoutSessionsByPlanId(int planId) {
        return workoutSessionDAO.getWorkoutSessionsByPlanId(planId);
    }

    public void updateWorkoutSession(WorkoutSessionEntity session) {
        new Thread(() -> workoutSessionDAO.updateWorkoutSession(session)).start();
    }

    public void deleteWorkoutSession(WorkoutSessionEntity session) {
        new Thread(() -> workoutSessionDAO.deleteWorkoutSession(session)).start();
    }

    public void deleteAllWorkoutSessions() {
        new Thread(() -> workoutSessionDAO.deleteAllWorkoutSessions()).start();
    }
}
