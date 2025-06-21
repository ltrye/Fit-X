package com.example.fitnestx.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fitnestx.data.entity.AuthProviderEntity;

import java.util.List;

@Dao
public interface AuthProviderDAO {
    @Query("SELECT * FROM AUTH_PROVIDER")
    LiveData<List<AuthProviderEntity>> getAllAuthProviders();

    @Query("SELECT * FROM AUTH_PROVIDER WHERE providerId = :providerId")
    AuthProviderEntity getAuthProviderById(int providerId);

    @Query("SELECT * FROM AUTH_PROVIDER WHERE userId = :userId")
    LiveData<List<AuthProviderEntity>> getAuthProvidersByUserId(int userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAuthProvider(AuthProviderEntity provider);

    @Update
    void updateAuthProvider(AuthProviderEntity provider);

    @Delete
    void deleteAuthProvider(AuthProviderEntity provider);

    @Query("DELETE FROM AUTH_PROVIDER")
    void deleteAllAuthProviders();
}
