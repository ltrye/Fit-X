package com.example.fitnestx.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fitnestx.data.entity.ExerciseEntity;
import com.example.fitnestx.data.entity.MuscleGroupEntity;

import java.util.List;

@Dao
public interface MuscleGroupDAO {
    @Query("SELECT * FROM MUSCLE_GROUP")
    LiveData<List<MuscleGroupEntity>> getAllMuscleGroups();
    @Query("SELECT * FROM MUSCLE_GROUP")
    List<MuscleGroupEntity> getAllList();
    @Query("SELECT * FROM MUSCLE_GROUP WHERE muscleGroupId = :muscleGroupId")
    MuscleGroupEntity getMuscleGroupById(int muscleGroupId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMuscleGroup(MuscleGroupEntity muscleGroup);

    @Update
    void updateMuscleGroup(MuscleGroupEntity muscleGroup);

    @Delete
    void deleteMuscleGroup(MuscleGroupEntity muscleGroup);

    @Query("DELETE FROM MUSCLE_GROUP")
    void deleteAllMuscleGroups();
}
