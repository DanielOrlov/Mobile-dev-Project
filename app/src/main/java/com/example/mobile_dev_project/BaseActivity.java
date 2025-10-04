package com.example.mobile_dev_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_dev_project.ui.login.LoginActivity;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base);


        Button homeButton = findViewById(R.id.btnHome);
        Button loginButton = findViewById(R.id.btnLogin);
        //Button profileButton = findViewById(R.id.btnProfile);
        Button locationButton = findViewById(R.id.btnLocation);
        Button userProfileButton = findViewById(R.id.btnUserProfile);
        Button reportConditionsButton = findViewById(R.id.btnReportConditions);
        Button settingsButton = findViewById(R.id.btnSettings);

        homeButton.setOnClickListener(v -> {
            if (!(this instanceof MainActivity)) {
                startActivity(new Intent(this, MainActivity.class));
            }
        });

        loginButton.setOnClickListener(v -> {
            if (!(this instanceof LoginActivity)) {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });

        settingsButton.setOnClickListener(v -> {
            // Placeholder for now
            Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show();
        });

//        userProfileButton.setOnClickListener(v -> {
//            if (!(this instanceof UserProfileActivity)) {
//                startActivity(new Intent(this, LoginActivity.class));
//            }
//        });
//        locationButton.setOnClickListener(v -> {
//            if (!(this instanceof UserProfileActivity)) {
//                startActivity(new Intent(this, LocationProfileActivity.class));
//            }
//        });
//        reportConditionsButton.setOnClickListener(v -> {
//            if (!(this instanceof UserProfileActivity)) {
//                startActivity(new Intent(this, ReportConditionsActivity.class));
//            }
//        });

//        findViewById(R.id.btnHome).setOnClickListener(v -> {
//            if (!(this instanceof MainActivity)) {
//                startActivity(new Intent(this, MainActivity.class));
//            }
//        });
//
//        findViewById(R.id.btnLogin).setOnClickListener(v -> {
//            if (!(this instanceof LoginActivity)) {
//                startActivity(new Intent(this, LoginActivity.class));
//            }
//        });
//
//        findViewById(R.id.btnSettings).setOnClickListener(v -> {
//            // Placeholder for now
//            Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show();
//        });


    }

    @Override
    public void setContentView(int layoutResID) {
        // Inflate the child layout into the content container
        getLayoutInflater().inflate(layoutResID, findViewById(R.id.content_container), true);
    }

}
