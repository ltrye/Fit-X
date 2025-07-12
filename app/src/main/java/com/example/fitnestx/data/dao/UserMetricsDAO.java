package com.example.fitnestx.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fitnestx.data.entity.UserMetricsEntity;

import java.util.List;

@Dao
public interface UserMetricsDAO {
    @Query("SELECT * FROM USER_METRICS")
    LiveData<List<UserMetricsEntity>> getAllUserMetrics();

    @Query("SELECT * FROM USER_METRICS WHERE metricId = :metricId")
    UserMetricsEntity getUserMetricById(int metricId);
    @Query("SELECT * FROM USER_METRICS WHERE userId = :userId")
    UserMetricsEntity getUserMetricByUserId(int userId);

    @Query("SELECT * FROM USER_METRICS WHERE userId = :userId")
    List<UserMetricsEntity> getAllUserMetricsByUserId(int userId);
    @Query("SELECT * FROM USER_METRICS WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    UserMetricsEntity getLatestUserMetricByUserId(int userId);

    @Query("SELECT * FROM USER_METRICS WHERE userId = :userId")
    LiveData<List<UserMetricsEntity>> getUserMetricsByUserId(int userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserMetric(UserMetricsEntity metric);

    @Update
    void updateUserMetric(UserMetricsEntity metric);

    @Delete
    void deleteUserMetric(UserMetricsEntity metric);

    @Query("DELETE FROM USER_METRICS")
    void deleteAllUserMetrics();

    @Query("SELECT COUNT(*) FROM user_metrics WHERE userId = :userId")
    int countMetricsForUser(int userId);
}
