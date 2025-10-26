package com.example.mobile_dev_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.mobile_dev_project.data.local.dao.LocationDao;
import com.example.mobile_dev_project.data.local.db.AppDatabase;
import com.example.mobile_dev_project.data.local.entity.Location;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MapActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // ✅ Initialize Room database here
        AppDatabase db = AppDatabase.getInstance(this);
        LocationDao locationDao = db.locationDao();

        Button loginButton = findViewById(R.id.btnLogin);
        Button locationButton = findViewById(R.id.btnLocation);
        Button userProfileButton = findViewById(R.id.btnUserProfile);
        Button reportConditionsButton = findViewById(R.id.btnReportConditions);

        loginButton.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        locationButton.setOnClickListener(v ->
                startActivity(new Intent(this, LocationProfileActivity.class)));

        userProfileButton.setOnClickListener(v ->
                startActivity(new Intent(this, UserProfileActivity.class)));

        reportConditionsButton.setOnClickListener(v ->
                startActivity(new Intent(this, ReportConditionsActivity.class)));
    }
}