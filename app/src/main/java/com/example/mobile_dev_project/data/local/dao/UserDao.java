package com.example.mobile_dev_project.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import com.example.mobile_dev_project.data.local.entity.User;

import java.util.List;

@Dao
public interface UserDao {

    @Upsert
    void upsert(User profile);   // Room 2.6+ (Java OK)

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    LiveData<User> observeByUid(String uid);

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    User getByUid(String uid);   // synchronous (use off main thread)

    @Query("SELECT * FROM users ORDER BY created_at DESC")
    LiveData<List<User>> observeAll();
}
