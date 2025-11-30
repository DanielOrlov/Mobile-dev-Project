package com.example.mobile_dev_project;

import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mobile_dev_project.BaseActivity;
import com.example.mobile_dev_project.data.LocationFeedbackRepository;
import com.example.mobile_dev_project.data.local.entity.LocationFeedback;

public class ReportConditionsActivity extends BaseActivity {

    private SeekBar seekBarNoise, seekBarFreeSpace, seekBarBusyness, seekBarWifi;
    private Switch switchSomething1, switchSomething2;
    private Button btnSubmit;
    private LocationFeedbackRepository feedbackRepository;
    private int locationId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_conditions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get location ID from intent
        locationId = getIntent().getIntExtra("location_id", -1);
        if (locationId == -1) {
            Toast.makeText(this, "Location ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        feedbackRepository = new LocationFeedbackRepository(this);

        // Initialize views
        seekBarNoise = findViewById(R.id.seekBar);
        seekBarFreeSpace = findViewById(R.id.seekBar2);
        seekBarBusyness = findViewById(R.id.seekBar3);
        seekBarWifi = findViewById(R.id.seekBar4);
        switchSomething1 = findViewById(R.id.switch2);
        switchSomething2 = findViewById(R.id.switch3);
        btnSubmit = findViewById(R.id.button);

        // Set default values (middle of range)
        if (seekBarNoise != null) {
            seekBarNoise.setMax(100);
            seekBarNoise.setProgress(50);
        }
        if (seekBarFreeSpace != null) {
            seekBarFreeSpace.setMax(100);
            seekBarFreeSpace.setProgress(50);
        }
        if (seekBarBusyness != null) {
            seekBarBusyness.setMax(100);
            seekBarBusyness.setProgress(50);
        }
        if (seekBarWifi != null) {
            seekBarWifi.setMax(100);
            seekBarWifi.setProgress(50);
        }

        // Submit button click listener
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> submitFeedback());
        }
    }

    private void submitFeedback() {
        if (locationId == -1) {
            Toast.makeText(this, "Invalid location", Toast.LENGTH_SHORT).show();
            return;
        }

        int noiseLevel = seekBarNoise != null ? seekBarNoise.getProgress() : 50;
        int freeSpace = seekBarFreeSpace != null ? seekBarFreeSpace.getProgress() : 50;
        int busyness = seekBarBusyness != null ? seekBarBusyness.getProgress() : 50;
        int wifiQuality = seekBarWifi != null ? seekBarWifi.getProgress() : 50;
        boolean something1Available = switchSomething1 != null && switchSomething1.isChecked();
        boolean something2Available = switchSomething2 != null && switchSomething2.isChecked();

        LocationFeedback feedback = new LocationFeedback(
            locationId,
            noiseLevel,
            wifiQuality,
            busyness,
            freeSpace,
            something1Available,
            something2Available
        );

        feedbackRepository.insertFeedback(feedback, insertedFeedback -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}