package com.example.fitnestx.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fitnestx.data.entity.WorkoutPlanEntity;

import java.util.List;

@Dao
public interface WorkoutPlanDAO {
    @Query("SELECT * FROM WORKOUT_PLAN")
    LiveData<List<WorkoutPlanEntity>> getAllWorkoutPlans();

    @Query("SELECT * FROM WORKOUT_PLAN WHERE planId = :planId")
    WorkoutPlanEntity getWorkoutPlanById(int planId);

    @Query("SELECT * FROM WORKOUT_PLAN WHERE userId = :userId")
    LiveData<List<WorkoutPlanEntity>> getWorkoutPlansByUserId(int userId);

    @Query("SELECT * FROM WORKOUT_PLAN WHERE userId = :userId ORDER BY planId DESC LIMIT 1")

    WorkoutPlanEntity getWorkoutPlanEntityByUserId(int userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWorkoutPlan(WorkoutPlanEntity plan);

    @Update
    void updateWorkoutPlan(WorkoutPlanEntity plan);

    @Delete
    void deleteWorkoutPlan(WorkoutPlanEntity plan);

    @Query("DELETE FROM WORKOUT_PLAN")
    void deleteAllWorkoutPlans();

    @Query("SELECT COUNT(*) FROM workout_plan WHERE userId = :userId AND isActive = 1")
    int countActivePlansForUser(int userId);
}
