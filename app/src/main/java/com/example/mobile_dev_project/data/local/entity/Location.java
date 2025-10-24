package com.example.mobile_dev_project.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Location {
    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "location_name")
    public String locationName;

    @ColumnInfo(name = "latitude")
    public double latitude;
    @ColumnInfo(name = "longitude")
    public double longitude;
}