package com.example.fitnestx.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fitnestx.data.entity.ExerciseEntity;

import java.util.List;

@Dao
public interface ExerciseDAO {
    @Query("SELECT * FROM EXERCISE")
    LiveData<List<ExerciseEntity>> getAllExercises();

    @Query("SELECT * FROM EXERCISE WHERE exerciseId = :exerciseId")
    ExerciseEntity getExerciseById(int exerciseId);

    @Query("SELECT * FROM EXERCISE WHERE muscleGroupId = :muscleGroupId")
    LiveData<List<ExerciseEntity>> getExercisesByMuscleGroupId(int muscleGroupId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertExercise(ExerciseEntity exercise);

    @Update
    void updateExercise(ExerciseEntity exercise);

    @Delete
    void deleteExercise(ExerciseEntity exercise);

    @Query("DELETE FROM EXERCISE")
    void deleteAllExercises();
}
