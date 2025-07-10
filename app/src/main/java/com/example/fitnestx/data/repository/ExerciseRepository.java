package com.example.fitnestx.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import com.example.fitnestx.data.AppDatabase;
import com.example.fitnestx.data.dao.ExerciseDAO;
import com.example.fitnestx.data.entity.ExerciseEntity;

public class ExerciseRepository {

    private final ExerciseDAO exerciseDAO;

    public ExerciseRepository(Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        exerciseDAO = appDatabase.exerciseDAO();
    }
    public String GetDesByExId(int exerciseId){
        return exerciseDAO.GetDesByExId(exerciseId);
    }

    public void insertExercise(ExerciseEntity exercise) {
        new Thread(() -> exerciseDAO.insertExercise(exercise)).start();
    }

    public List<ExerciseEntity> getAllExercises() {
        return exerciseDAO.getListOfExercises();
    }

    public ExerciseEntity getExerciseById(int exerciseId) {
        return exerciseDAO.getExerciseById(exerciseId);
    }

    public LiveData<List<ExerciseEntity>> getExercisesByMuscleGroupId(int muscleGroupId) {
        return exerciseDAO.getExercisesByMuscleGroupId(muscleGroupId);
    }

    public void updateExercise(ExerciseEntity exercise) {
        new Thread(() -> exerciseDAO.updateExercise(exercise)).start();
    }

    public void deleteExercise(ExerciseEntity exercise) {
        new Thread(() -> exerciseDAO.deleteExercise(exercise)).start();
    }

    public void deleteAllExercises() {
        new Thread(() -> exerciseDAO.deleteAllExercises()).start();
    }
}
