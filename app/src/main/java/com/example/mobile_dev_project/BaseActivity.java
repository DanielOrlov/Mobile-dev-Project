package com.example.mobile_dev_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_dev_project.LoginActivity;
import com.example.mobile_dev_project.LocationProfileActivity;
import com.example.mobile_dev_project.MainActivity;
import com.example.mobile_dev_project.ReportConditionsActivity;
import com.example.mobile_dev_project.UserProfileActivity;
import com.example.mobile_dev_project.MapActivity;

/**
 * BaseActivity: Provides a common navigation bar for all activities.
 * Buttons navigate to other activities if they exist.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This assumes your base layout has a container with id "content_container"
        super.setContentView(R.layout.activity_base);

        // Navbar buttons
        setupNavBarButton(R.id.btnHome, MainActivity.class);
        setupNavBarButton(R.id.btnMap, MapActivity.class);
        setupNavBarButton(R.id.btnProfile, UserProfileActivity.class);
        setupNavBarButton(R.id.btnLocation, LocationProfileActivity.class);
        setupNavBarButton(R.id.btnReportConditions, ReportConditionsActivity.class);
        setupNavBarButton(R.id.btnLogin, LoginActivity.class);

        // Settings button (placeholder)
        Button settingsButton = findViewById(R.id.btnSettings);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v ->
                    Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Helper method to safely set up a navbar button click listener
     */
    private void setupNavBarButton(int buttonId, Class<?> targetActivity) {
        Button button = findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(v -> {
                if (!this.getClass().equals(targetActivity)) {
                    try {
                        startActivity(new Intent(this, targetActivity));
                    } catch (Exception e) {
                        Toast.makeText(this,
                                targetActivity.getSimpleName() + " not implemented yet.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        // Inflate the child layout into the content container
        getLayoutInflater().inflate(layoutResID, findViewById(R.id.content_container), true);
    }
}