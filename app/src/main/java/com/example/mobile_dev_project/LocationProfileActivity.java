package com.example.mobile_dev_project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LocationProfileActivity extends BaseActivity {

    private TextView locationName, locationAddress;
    private Button btnCheckIn, btnFavorite, btnFindBuddy;
    private ImageView image1, image2, image3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_profile);

        // Bind views safely
        locationName = findViewById(R.id.locationName);
        locationAddress = findViewById(R.id.locationAddress);

        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnFindBuddy = findViewById(R.id.btnFindBuddy);

        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);


        if (btnCheckIn != null) {
            btnCheckIn.setOnClickListener(v ->
                    Toast.makeText(this, "Checked in!", Toast.LENGTH_SHORT).show());
        }

        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v ->
                    Toast.makeText(this, "Added to Favorites!", Toast.LENGTH_SHORT).show());
        }

        if (btnFindBuddy != null) {
            btnFindBuddy.setOnClickListener(v ->
                    Toast.makeText(this, "Finding a Buddy...", Toast.LENGTH_SHORT).show());
        }
    }
}