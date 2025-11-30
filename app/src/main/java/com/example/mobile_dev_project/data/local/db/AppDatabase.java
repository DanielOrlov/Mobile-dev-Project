package com.example.mobile_dev_project.data.local.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mobile_dev_project.data.local.dao.LocationDao;
import com.example.mobile_dev_project.data.local.dao.LocationFeedbackDao;
import com.example.mobile_dev_project.data.local.dao.UserDao;
import com.example.mobile_dev_project.data.local.entity.Location;
import com.example.mobile_dev_project.data.local.entity.LocationFeedback;
import com.example.mobile_dev_project.data.local.entity.User;

@Database(entities = {Location.class, User.class, LocationFeedback.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LocationDao locationDao();
    public abstract UserDao userDao();
    public abstract LocationFeedbackDao locationFeedbackDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "app_database"
                    )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
