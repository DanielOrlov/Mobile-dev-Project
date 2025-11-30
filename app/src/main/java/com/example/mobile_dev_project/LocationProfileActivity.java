package com.example.mobile_dev_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobile_dev_project.data.LocationFeedbackRepository;
import com.example.mobile_dev_project.data.LocationRepository;
import com.example.mobile_dev_project.data.local.entity.Location;

public class LocationProfileActivity extends BaseActivity {

    private TextView locationName, locationAddress, locationPhone, locationEmail, locationDescription;
    private TextView feedbackSummary, feedbackNoiseLevel, feedbackWifiQuality, feedbackBusyness, 
                     feedbackFreeSpace, feedbackAmenities, feedbackCount;
    private LinearLayout feedbackDetails;
    private Button btnCheckIn, btnFavorite, btnFindBuddy, btnLeaveFeedback;
    private ImageView image1, image2, image3;
    private LocationRepository locationRepository;
    private LocationFeedbackRepository feedbackRepository;
    private int locationId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_profile);

        locationRepository = new LocationRepository(this);
        feedbackRepository = new LocationFeedbackRepository(this);

        // Bind views safely
        locationName = findViewById(R.id.locationName);
        locationAddress = findViewById(R.id.locationAddress);
        locationPhone = findViewById(R.id.locationPhone);
        locationEmail = findViewById(R.id.locationEmail);
        locationDescription = findViewById(R.id.locationDescription);

        feedbackSummary = findViewById(R.id.feedbackSummary);
        feedbackNoiseLevel = findViewById(R.id.feedbackNoiseLevel);
        feedbackWifiQuality = findViewById(R.id.feedbackWifiQuality);
        feedbackBusyness = findViewById(R.id.feedbackBusyness);
        feedbackFreeSpace = findViewById(R.id.feedbackFreeSpace);
        feedbackAmenities = findViewById(R.id.feedbackAmenities);
        feedbackCount = findViewById(R.id.feedbackCount);
        feedbackDetails = findViewById(R.id.feedbackDetails);

        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnFindBuddy = findViewById(R.id.btnFindBuddy);
        btnLeaveFeedback = findViewById(R.id.btnLeaveFeedback);

        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);

        // Get location ID from intent
        locationId = getIntent().getIntExtra("location_id", -1);
        
        if (locationId != -1) {
            loadLocationData(locationId);
            loadFeedbackData(locationId);
        } else {
            // If no location ID provided, show default/placeholder data
            if (locationName != null) {
                locationName.setText("Location Name");
            }
            if (locationAddress != null) {
                locationAddress.setText("Location Address");
            }
        }

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

        if (btnLeaveFeedback != null) {
            btnLeaveFeedback.setOnClickListener(v -> {
                if (locationId != -1) {
                    Intent intent = new Intent(this, ReportConditionsActivity.class);
                    intent.putExtra("location_id", locationId);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Location ID not available", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload feedback when returning from ReportConditionsActivity
        if (locationId != -1) {
            loadFeedbackData(locationId);
        }
    }

    private void loadLocationData(int locationId) {
        locationRepository.getLocationById(locationId, location -> {
            runOnUiThread(() -> {
                if (location != null) {
                    // Set location name
                    if (locationName != null) {
                        locationName.setText(location.locationName);
                    }
                    
                    // Set address (use address field if available, otherwise show coordinates)
                    if (locationAddress != null) {
                        if (location.address != null && !location.address.trim().isEmpty()) {
                            locationAddress.setText(location.address);
                        } else {
                            locationAddress.setText(String.format("Coordinates: %.6f, %.6f", 
                                    location.latitude, location.longitude));
                        }
                    }
                    
                    // Set phone
                    if (locationPhone != null) {
                        if (location.phone != null && !location.phone.trim().isEmpty()) {
                            locationPhone.setText("Phone: " + location.phone);
                            locationPhone.setVisibility(View.VISIBLE);
                        } else {
                            locationPhone.setVisibility(View.GONE);
                        }
                    }
                    
                    // Set email
                    if (locationEmail != null) {
                        if (location.email != null && !location.email.trim().isEmpty()) {
                            locationEmail.setText("Email: " + location.email);
                            locationEmail.setVisibility(View.VISIBLE);
                        } else {
                            locationEmail.setVisibility(View.GONE);
                        }
                    }
                    
                    // Set description
                    if (locationDescription != null) {
                        if (location.description != null && !location.description.trim().isEmpty()) {
                            locationDescription.setText("Description: " + location.description);
                            locationDescription.setVisibility(View.VISIBLE);
                        } else {
                            locationDescription.setVisibility(View.GONE);
                        }
                    }
                } else {
                    Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadFeedbackData(int locationId) {
        feedbackRepository.getAverageFeedback(locationId, averages -> {
            runOnUiThread(() -> {
                if (averages.totalFeedbackCount > 0) {
                    // Show feedback details
                    if (feedbackDetails != null) {
                        feedbackDetails.setVisibility(View.VISIBLE);
                    }
                    if (feedbackSummary != null) {
                        feedbackSummary.setVisibility(View.GONE);
                    }

                    // Update feedback values
                    if (feedbackNoiseLevel != null) {
                        String noiseLabel = getNoiseLabel(averages.avgNoiseLevel);
                        feedbackNoiseLevel.setText(String.format("Noise Level: %.0f/100 (%s)", 
                            averages.avgNoiseLevel, noiseLabel));
                    }

                    if (feedbackWifiQuality != null) {
                        String wifiLabel = getWifiLabel(averages.avgWifiQuality);
                        feedbackWifiQuality.setText(String.format("Wi-Fi Quality: %.0f/100 (%s)", 
                            averages.avgWifiQuality, wifiLabel));
                    }

                    if (feedbackBusyness != null) {
                        String busynessLabel = getBusynessLabel(averages.avgBusyness);
                        feedbackBusyness.setText(String.format("Busyness: %.0f/100 (%s)", 
                            averages.avgBusyness, busynessLabel));
                    }

                    if (feedbackFreeSpace != null) {
                        String freeSpaceLabel = getFreeSpaceLabel(averages.avgFreeSpace);
                        feedbackFreeSpace.setText(String.format("Free Space: %.0f/100 (%s)", 
                            averages.avgFreeSpace, freeSpaceLabel));
                    }

                    if (feedbackAmenities != null) {
                        StringBuilder amenities = new StringBuilder("Amenities: ");
                        if (averages.something1AvailableCount > 0) {
                            amenities.append("Something #1 (").append(averages.something1AvailableCount).append(")");
                        }
                        if (averages.something2AvailableCount > 0) {
                            if (amenities.length() > 10) amenities.append(", ");
                            amenities.append("Something #2 (").append(averages.something2AvailableCount).append(")");
                        }
                        if (amenities.length() == 10) {
                            amenities.append("None reported");
                        }
                        feedbackAmenities.setText(amenities.toString());
                    }

                    if (feedbackCount != null) {
                        feedbackCount.setText(String.format("Based on %d feedback%s", 
                            averages.totalFeedbackCount, 
                            averages.totalFeedbackCount == 1 ? "" : "s"));
                    }
                } else {
                    // No feedback yet
                    if (feedbackDetails != null) {
                        feedbackDetails.setVisibility(View.GONE);
                    }
                    if (feedbackSummary != null) {
                        feedbackSummary.setVisibility(View.VISIBLE);
                        feedbackSummary.setText("No feedback yet. Be the first to leave feedback!");
                    }
                }
            });
        });
    }

    private String getNoiseLabel(double value) {
        if (value < 25) return "Quiet";
        if (value < 50) return "Medium";
        if (value < 75) return "Loud";
        return "Very Loud";
    }

    private String getWifiLabel(double value) {
        if (value < 25) return "None";
        if (value < 50) return "Acceptable";
        if (value < 75) return "Good";
        return "Fast";
    }

    private String getBusynessLabel(double value) {
        if (value < 25) return "Not a soul";
        if (value < 50) return "Lively";
        if (value < 75) return "Packed";
        return "Very Packed";
    }

    private String getFreeSpaceLabel(double value) {
        if (value < 25) return "Very tight";
        if (value < 50) return "Regular";
        if (value < 75) return "Spacious";
        return "Very Spacious";
    }
}