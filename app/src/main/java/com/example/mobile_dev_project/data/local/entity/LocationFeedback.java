package com.example.mobile_dev_project.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "location_feedback",
    foreignKeys = @ForeignKey(
        entity = Location.class,
        parentColumns = "uid",
        childColumns = "location_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("location_id")}
)
public class LocationFeedback {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "location_id")
    public int locationId;

    @ColumnInfo(name = "noise_level")
    public int noiseLevel; // 0-100

    @ColumnInfo(name = "wifi_quality")
    public int wifiQuality; // 0-100

    @ColumnInfo(name = "busyness")
    public int busyness; // 0-100

    @ColumnInfo(name = "free_space")
    public int freeSpace; // 0-100

    @ColumnInfo(name = "something1_available")
    public boolean something1Available;

    @ColumnInfo(name = "something2_available")
    public boolean something2Available;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    // Default constructor for Room
    public LocationFeedback() {}

    public LocationFeedback(int locationId, int noiseLevel, int wifiQuality, int busyness, 
                           int freeSpace, boolean something1Available, boolean something2Available) {
        this.locationId = locationId;
        this.noiseLevel = noiseLevel;
        this.wifiQuality = wifiQuality;
        this.busyness = busyness;
        this.freeSpace = freeSpace;
        this.something1Available = something1Available;
        this.something2Available = something2Available;
        this.createdAt = System.currentTimeMillis();
    }
}

