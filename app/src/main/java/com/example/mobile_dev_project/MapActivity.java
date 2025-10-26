package com.example.mobile_dev_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.mobile_dev_project.data.local.dao.LocationDao;
import com.example.mobile_dev_project.data.local.db.AppDatabase;

public class MapActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // ✅ Initialize Room database here
        AppDatabase db = AppDatabase.getInstance(this);
        LocationDao locationDao = db.locationDao();

        Button locationButton = findViewById(R.id.btnLocation);
        Button reportConditionsButton = findViewById(R.id.btnReportConditions);

        locationButton.setOnClickListener(v ->
                startActivity(new Intent(this, LocationProfileActivity.class)));

        reportConditionsButton.setOnClickListener(v ->
                startActivity(new Intent(this, ReportConditionsActivity.class)));
    }
}