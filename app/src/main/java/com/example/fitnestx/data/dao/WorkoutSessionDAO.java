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
    @Query("SELECT * FROM WORKOUT_SESSION")
    LiveData<List<WorkoutSessionEntity>> getAllWorkoutSessions();

    @Query("SELECT * FROM WORKOUT_SESSION WHERE sessionId = :sessionId")
    WorkoutSessionEntity getWorkoutSessionById(int sessionId);

    @Query("SELECT * FROM WORKOUT_SESSION WHERE planId = :planId")
    LiveData<List<WorkoutSessionEntity>> getWorkoutSessionsByPlanId(int planId);

    @Query("SELECT * FROM WORKOUT_SESSION WHERE planId = :planId")
    List<WorkoutSessionEntity> getWorkoutSessionsByPlanIdList(int planId);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWorkoutSession(WorkoutSessionEntity session);

    @Update
    void updateWorkoutSession(WorkoutSessionEntity session);

    @Delete
    void deleteWorkoutSession(WorkoutSessionEntity session);

    @Query("DELETE FROM WORKOUT_SESSION")
    void deleteAllWorkoutSessions();
}
