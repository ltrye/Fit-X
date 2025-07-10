package com.example.fitnestx.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fitnestx.data.entity.WorkoutSessionEntity;

import java.util.List;

@Dao
public interface WorkoutSessionDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Ensure REPLACE strategy for inserts
    void insertWorkoutSession(WorkoutSessionEntity session);

    @Update
    void updateWorkoutSession(WorkoutSessionEntity session);

    @Delete
    void deleteWorkoutSession(WorkoutSessionEntity session);

    @Query("SELECT * FROM WORKOUT_SESSION")
    LiveData<List<WorkoutSessionEntity>> getAllWorkoutSessions();

    @Query("SELECT * FROM WORKOUT_SESSION WHERE sessionId = :sessionId")
    WorkoutSessionEntity getWorkoutSessionById(int sessionId);

    @Query("SELECT * FROM WORKOUT_SESSION WHERE planId = :planId")
    List<WorkoutSessionEntity> getWorkoutSessionsByPlanIdList(int planId);


    @Query("DELETE FROM WORKOUT_SESSION WHERE planId =:planId")
    void deleteAllWorkoutSessionsForPlan(int planId);
    @Query("DELETE FROM WORKOUT_SESSION")
    void deleteAllWorkoutSessions();

    // New method to update all sessions' completion status for a given plan
    @Query("UPDATE WORKOUT_SESSION SET isCompleted = :isCompleted WHERE planId = :planId")
    void updateAllSessionsCompletionStatus(int planId, boolean isCompleted);
}
