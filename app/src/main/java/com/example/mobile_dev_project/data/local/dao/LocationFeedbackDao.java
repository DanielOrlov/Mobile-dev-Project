package com.example.mobile_dev_project.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.mobile_dev_project.data.local.entity.LocationFeedback;

import java.util.List;

@Dao
public interface LocationFeedbackDao {
    @Insert
    void insert(LocationFeedback feedback);

    @Query("SELECT * FROM location_feedback WHERE location_id = :locationId ORDER BY created_at DESC")
    List<LocationFeedback> getFeedbacksByLocationId(int locationId);

    @Query("SELECT AVG(noise_level) FROM location_feedback WHERE location_id = :locationId")
    Double getAverageNoiseLevel(int locationId);

    @Query("SELECT AVG(wifi_quality) FROM location_feedback WHERE location_id = :locationId")
    Double getAverageWifiQuality(int locationId);

    @Query("SELECT AVG(busyness) FROM location_feedback WHERE location_id = :locationId")
    Double getAverageBusyness(int locationId);

    @Query("SELECT AVG(free_space) FROM location_feedback WHERE location_id = :locationId")
    Double getAverageFreeSpace(int locationId);

    @Query("SELECT COUNT(*) FROM location_feedback WHERE location_id = :locationId AND something1_available = 1")
    int getSomething1AvailableCount(int locationId);

    @Query("SELECT COUNT(*) FROM location_feedback WHERE location_id = :locationId AND something2_available = 1")
    int getSomething2AvailableCount(int locationId);

    @Query("SELECT COUNT(*) FROM location_feedback WHERE location_id = :locationId")
    int getFeedbackCount(int locationId);
}

