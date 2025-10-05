package com.example.mobile_dev_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.example.mobile_dev_project.LocationProfileActivity;
import com.example.mobile_dev_project.BaseActivity;
import com.example.mobile_dev_project.MapActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //EdgeToEdge.enable(this);

        Button loginButton = findViewById(R.id.btnLogin);
        Button profileButton = findViewById(R.id.btnProfile);
        Button locationButton = findViewById(R.id.btnLocation);
        Button userProfileButton = findViewById(R.id.btnUserProfile);
        Button reportConditionsButton = findViewById(R.id.btnReportConditions);
        Button mapButton = findViewById(R.id.btnMap);
        mapButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MapActivity.class));
        });
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        locationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, LocationProfileActivity.class);
                startActivity(intent);
            }
        });

        userProfileButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });

        reportConditionsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, ReportConditionsActivity.class);
                startActivity(intent);
            }
        });
    }
}