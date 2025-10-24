package com.example.mobile_dev_project.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.mobile_dev_project.data.local.entity.Location;

import java.util.List;

@Dao
public interface LocationDao {
    @Query("SELECT * FROM location")
    List<Location> getAll();

    @Query("SELECT * FROM location WHERE uid IN (:locationIds)")
    List<Location> loadAllByIds(int[] locationIds);

//    @Query("SELECT * FROM location WHERE location_name LIKE :location_name)
//    Location findByName(String name);

    @Insert
    void insert(Location location);

    @Insert
    void insertAll(Location... locations);

    @Delete
    void delete(Location location);
}
