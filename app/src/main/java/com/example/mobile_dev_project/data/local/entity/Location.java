package com.example.mobile_dev_project.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Location {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "location_name")
    public String locationName;

    @ColumnInfo(name = "latitude")
    public double latitude;
    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "phone")
    public String phone;

    @ColumnInfo(name = "address")
    public String address;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "description")
    public String description;

    // Default constructor for Room
    public Location() {}

    // Constructor for creating locations
    @Ignore
    public Location(String locationName, double latitude, double longitude) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = "";
        this.address = "";
        this.email = "";
        this.description = "";
    }

    // Full constructor with contact information
    @Ignore
    public Location(String locationName, double latitude, double longitude, String phone, String address, String email, String description) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone != null ? phone : "";
        this.address = address != null ? address : "";
        this.email = email != null ? email : "";
        this.description = description != null ? description : "";
    }
}