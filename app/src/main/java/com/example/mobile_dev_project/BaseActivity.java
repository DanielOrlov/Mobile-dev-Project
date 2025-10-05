package com.example.mobile_dev_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * BaseActivity: Provides a common navigation bar for all activities.
 * Buttons navigate to other activities if they exist.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base);

        // Navbar buttons
        setupNavBarButton(R.id.btnLocation, LocationProfileActivity.class);
        setupNavBarButton(R.id.btnReportConditions, ReportConditionsActivity.class);
        setupNavBarButton(R.id.btnLogin, LoginActivity.class);
        setupNavBarButton(R.id.buttonToGoHome, MapActivity.class);
        // Settings button (placeholder)
        Button settingsButton = findViewById(R.id.btnSettings);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v ->
                    Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show());
        }
    }

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
        getLayoutInflater().inflate(layoutResID, findViewById(R.id.content_container), true);
    }
}