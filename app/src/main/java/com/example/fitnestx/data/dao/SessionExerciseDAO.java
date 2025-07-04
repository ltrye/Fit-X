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
    @Query("SELECT * FROM SESSION_EXERCISE")
    LiveData<List<SessionExerciseEntity>> getAllSessionExercises();
    @Query("SELECT exerciseId FROM SESSION_EXERCISE WHERE sessionId = :sessionId")
    List<Integer> GetListIdExercsieBySessionId(int sessionId);

    @Query("SELECT * FROM SESSION_EXERCISE WHERE sessionId = :sessionId AND exerciseId = :exerciseId")
    SessionExerciseEntity getSessionExercise(int sessionId, int exerciseId);

    @Query("SELECT * FROM SESSION_EXERCISE WHERE sessionId = :sessionId")
    LiveData<List<SessionExerciseEntity>> getExercisesBySessionId(int sessionId);
    @Query("SELECT COUNT(*) FROM SESSION_EXERCISE WHERE sessionId = :sessionId")
    int TotalSessionExercise(int sessionId);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSessionExercise(SessionExerciseEntity sessionExercise);

    @Update
    void updateSessionExercise(SessionExerciseEntity sessionExercise);

    @Delete
    void deleteSessionExercise(SessionExerciseEntity sessionExercise);

    @Query("DELETE FROM SESSION_EXERCISE")
    void deleteAllSessionExercises();


}
