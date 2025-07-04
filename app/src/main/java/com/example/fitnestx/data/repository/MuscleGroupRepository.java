package com.example.fitnestx.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import com.example.fitnestx.data.AppDatabase;
import com.example.fitnestx.data.dao.MuscleGroupDAO;
import com.example.fitnestx.data.entity.MuscleGroupEntity;

public class MuscleGroupRepository {

    private final MuscleGroupDAO muscleGroupDAO;

    public MuscleGroupRepository(Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        muscleGroupDAO = appDatabase.muscleGroupDAO();
    }
    public List<MuscleGroupEntity> getListMuscleGroup() {
        return muscleGroupDAO.getAllList();
    }
    public void insertMuscleGroup(MuscleGroupEntity muscleGroup) {
        new Thread(() -> muscleGroupDAO.insertMuscleGroup(muscleGroup)).start();
    }

    public LiveData<List<MuscleGroupEntity>> getAllMuscleGroups() {
        return muscleGroupDAO.getAllMuscleGroups();
    }

    public MuscleGroupEntity getMuscleGroupById(int muscleGroupId) {
        return muscleGroupDAO.getMuscleGroupById(muscleGroupId);
    }

    public void updateMuscleGroup(MuscleGroupEntity muscleGroup) {
        new Thread(() -> muscleGroupDAO.updateMuscleGroup(muscleGroup)).start();
    }

    public void deleteMuscleGroup(MuscleGroupEntity muscleGroup) {
        new Thread(() -> muscleGroupDAO.deleteMuscleGroup(muscleGroup)).start();
    }

    public void deleteAllMuscleGroups() {
        new Thread(() -> muscleGroupDAO.deleteAllMuscleGroups()).start();
    }
}
