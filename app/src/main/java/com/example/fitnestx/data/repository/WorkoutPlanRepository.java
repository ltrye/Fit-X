package com.example.fitnestx.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import com.example.fitnestx.data.AppDatabase;
import com.example.fitnestx.data.dao.WorkoutPlanDAO;
import com.example.fitnestx.data.entity.WorkoutPlanEntity;

public class WorkoutPlanRepository {

    private final WorkoutPlanDAO workoutPlanDAO;

    public WorkoutPlanRepository(Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        workoutPlanDAO = appDatabase.workoutPlanDAO();
    }

    public void insertWorkoutPlan(WorkoutPlanEntity plan) {
        new Thread(() -> workoutPlanDAO.insertWorkoutPlan(plan)).start();
    }

    public LiveData<List<WorkoutPlanEntity>> getAllWorkoutPlans() {
        return workoutPlanDAO.getAllWorkoutPlans();
    }

    public WorkoutPlanEntity getWorkoutPlanById(int planId) {
        return workoutPlanDAO.getWorkoutPlanById(planId);
    }

    public WorkoutPlanEntity getWorkoutPlansByUserId(int userId) {
        return workoutPlanDAO.getWorkoutPlanEntityByUserId(userId);
    }

    public void updateWorkoutPlan(WorkoutPlanEntity plan) {
        new Thread(() -> workoutPlanDAO.updateWorkoutPlan(plan)).start();
    }

    public void deleteWorkoutPlan(WorkoutPlanEntity plan) {
        new Thread(() -> workoutPlanDAO.deleteWorkoutPlan(plan)).start();
    }

    public void deleteAllWorkoutPlans() {
        new Thread(() -> workoutPlanDAO.deleteAllWorkoutPlans()).start();
    }

    public boolean hasActivePlanForUser(int userId) {
        return workoutPlanDAO.countActivePlansForUser(userId) > 0;
    }
}
