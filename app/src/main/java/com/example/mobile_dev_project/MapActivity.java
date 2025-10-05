package com.example.mobile_dev_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MapActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

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