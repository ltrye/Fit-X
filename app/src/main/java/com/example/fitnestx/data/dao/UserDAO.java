package com.example.fitnestx.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fitnestx.data.entity.UserEntity;

import java.util.List;

@Dao
public interface UserDAO {
    @Query("SELECT * FROM USER")
    LiveData<List<UserEntity>> getAllUsers();

    @Query("SELECT * FROM USER WHERE id = :userId")
    UserEntity getUserById(int userId);

    @Query("SELECT * FROM USER WHERE isActive = 1")
    LiveData<List<UserEntity>> getActiveUsers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);

    @Update
    void updateUser(UserEntity user);

    @Delete
    void deleteUser(UserEntity user);

    @Query("DELETE FROM USER")
    void deleteAllUsers();
}
