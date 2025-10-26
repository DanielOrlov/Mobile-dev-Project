package com.example.mobile_dev_project;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.example.mobile_dev_project.data.LocationRepository;
import com.example.mobile_dev_project.data.local.entity.Location;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MapActivity - Shows Google Map with location markers
 */
public class MapActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRepository locationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRepository = new LocationRepository(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setupButtons();
        addDummyLocations();
    }

    private void setupButtons() {
        Button locationButton = findViewById(R.id.btnLocation);
        Button reportConditionsButton = findViewById(R.id.btnReportConditions);
        Button addLocationButton = findViewById(R.id.btnAddLocation);
        Button zoomInButton = findViewById(R.id.btnZoomIn);
        Button zoomOutButton = findViewById(R.id.btnZoomOut);

        if (locationButton != null) {
            locationButton.setOnClickListener(v ->
                    startActivity(new Intent(this, LocationProfileActivity.class)));
        }

        if (reportConditionsButton != null) {
            reportConditionsButton.setOnClickListener(v ->
                    startActivity(new Intent(this, ReportConditionsActivity.class)));
        }

        if (addLocationButton != null) {
            addLocationButton.setOnClickListener(v -> showAddLocationDialog());
        }

        if (zoomInButton != null) {
            zoomInButton.setOnClickListener(v -> zoomIn());
        }

        if (zoomOutButton != null) {
            zoomOutButton.setOnClickListener(v -> zoomOut());
        }
    }

    private void addDummyLocations() {
        Location[] dummyLocations = {
            new Location("CN Tower", 43.6426, -79.3871),
            new Location("Royal Ontario Museum", 43.6677, -79.3948),
            new Location("Toronto Islands", 43.6256, -79.3847),
            new Location("Casa Loma", 43.6780, -79.4095),
            new Location("Distillery District", 43.6509, -79.3602)
        };

        locationRepository.insertAllLocations(dummyLocations, locations -> {
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            } else {
                requestLocationPermission();
            }

            LatLng torontoLocation = new LatLng(43.6532, -79.3832);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(torontoLocation, 10));

            googleMap.setOnMarkerClickListener(this);
            loadLocationsAndAddMarkers();

        } catch (Exception e) {
            Toast.makeText(this, "Map error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Location permission is needed to show your current location", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (googleMap != null) {
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                googleMap.setMyLocationEnabled(true);
                            }
                        }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, (android.location.Location location) -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        }
                    });
        }
    }

    private void loadLocationsAndAddMarkers() {
        locationRepository.getAllLocations(locations -> {
            runOnUiThread(() -> {
                if (googleMap != null) {
                    googleMap.clear();
                    for (Location location : locations) {
                        LatLng latLng = new LatLng(location.latitude, location.longitude);
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(latLng)
                                .title(location.locationName);
                        googleMap.addMarker(markerOptions);
                    }
                }
            });
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, "Clicked: " + marker.getTitle(), Toast.LENGTH_SHORT).show();
        return false;
    }

    private void showAddLocationDialog() {
        AddLocationDialog.show(this, locationRepository, () -> {
            loadLocationsAndAddMarkers();
        });
    }

    private void zoomIn() {
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        }
    }

    private void zoomOut() {
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.zoomOut());
        }
    }
}