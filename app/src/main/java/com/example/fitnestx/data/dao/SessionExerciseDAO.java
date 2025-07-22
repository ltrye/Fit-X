package com.example.fitnestx.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fitnestx.data.entity.SessionExerciseEntity;

import java.util.List;

@Dao
public interface SessionExerciseDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Ensure REPLACE strategy for inserts
    void insertSessionExercise(SessionExerciseEntity sessionExercise);

    @Update
    void updateSessionExercise(SessionExerciseEntity sessionExercise);

    @Delete
    void deleteSessionExercise(SessionExerciseEntity sessionExercise);
    @Query("SELECT * FROM SESSION_EXERCISE WHERE isMarked=0 AND sessionId =:sessionId")
    List<SessionExerciseEntity> getIncompletedExercisesInSession(int sessionId);

    @Query("SELECT * FROM SESSION_EXERCISE")
    LiveData<List<SessionExerciseEntity>> getAllSessionExercises();

    @Query("SELECT exerciseId FROM SESSION_EXERCISE WHERE sessionId = :sessionId")
    List<Integer> GetListIdExercsieBySessionId(int sessionId);

    @Query("SELECT * FROM SESSION_EXERCISE WHERE sessionId = :sessionId AND exerciseId = :exerciseId")
    SessionExerciseEntity getSessionExercise(int sessionId, int exerciseId);

    @Query("SELECT * FROM SESSION_EXERCISE WHERE sessionId = :sessionId")
    LiveData<List<SessionExerciseEntity>> getExercisesBySessionId(int sessionId);

    @Query("SELECT * FROM SESSION_EXERCISE WHERE sessionId = :sessionId")
    List<SessionExerciseEntity> getExercisesListBySessionId(int sessionId);

    @Query("SELECT COUNT(*) FROM SESSION_EXERCISE WHERE sessionId = :sessionId")
    int TotalSessionExercise(int sessionId);

    @Query("DELETE FROM SESSION_EXERCISE")
    void deleteAllSessionExercises();

    // New method to update all exercises' marked status for a given plan
    @Query("UPDATE SESSION_EXERCISE SET isMarked = :isMarked WHERE sessionId IN (SELECT sessionId FROM WORKOUT_SESSION WHERE planId = :planId)")
    void updateAllExercisesMarkedStatusForPlan(int planId, boolean isMarked);

    // New method to delete all session exercises for a given plan
    @Query("DELETE FROM SESSION_EXERCISE WHERE sessionId IN (SELECT sessionId FROM WORKOUT_SESSION WHERE planId = :planId)")
    void deleteAllSessionExercisesForPlan(int planId);

    // New method to delete all session exercises for a specific session
    @Query("DELETE FROM SESSION_EXERCISE WHERE sessionId = :sessionId")
    void deleteSessionExercisesBySessionId(int sessionId);
}
