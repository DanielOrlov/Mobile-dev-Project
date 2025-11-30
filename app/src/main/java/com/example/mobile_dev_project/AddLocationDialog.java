package com.example.mobile_dev_project;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.Place.Field;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;

import com.example.mobile_dev_project.data.LocationRepository;
import com.example.mobile_dev_project.data.local.entity.Location;

import java.util.Arrays;
import java.util.List;


public class AddLocationDialog {

    public interface OnLocationAddedListener {
        void onLocationAdded(Location location);
    }

    public static void show(Context context, LocationRepository repository, OnLocationAddedListener listener) {
        // Get GPS location as default
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        double lat = location != null ? location.getLatitude() : 43.6532;
                        double lng = location != null ? location.getLongitude() : -79.3832;
                        showAtPoint(context, repository, lat, lng, listener);
                    })
                    .addOnFailureListener(e -> {
                        showAtPoint(context, repository, 43.6532, -79.3832, listener);
                    });
        } else {
            showAtPoint(context, repository, 43.6532, -79.3832, listener);
        }
    }
    
    public static void showAtPlace(Context context, LocationRepository repository, Place place, double latitude, double longitude, OnLocationAddedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_location_search, null);

        AutoCompleteTextView searchEditText = dialogView.findViewById(R.id.editTextSearchLocation);
        EditText nameEditText = dialogView.findViewById(R.id.editTextLocationName);
        EditText phoneEditText = dialogView.findViewById(R.id.editTextPhone);
        EditText addressEditText = dialogView.findViewById(R.id.editTextAddress);
        EditText emailEditText = dialogView.findViewById(R.id.editTextEmail);
        EditText descriptionEditText = dialogView.findViewById(R.id.editTextDescription);
        EditText latitudeEditText = dialogView.findViewById(R.id.editTextLatitude);
        EditText longitudeEditText = dialogView.findViewById(R.id.editTextLongitude);
        TextView gpsCoordinatesTextView = dialogView.findViewById(R.id.textViewGpsCoordinates);
        Button addButton = dialogView.findViewById(R.id.btnAdd);
        Button cancelButton = dialogView.findViewById(R.id.btnCancel);

        // Use provided coordinates and place information
        final double[] currentLatitude = {latitude};
        final double[] currentLongitude = {longitude};
        final boolean[] locationObtained = {true};
        
        // Set coordinates
        latitudeEditText.setText(String.valueOf(latitude));
        longitudeEditText.setText(String.valueOf(longitude));
        
        // Auto-fill from place
        if (place.getName() != null && nameEditText != null) {
            nameEditText.setText(place.getName());
        }
        if (place.getAddress() != null && addressEditText != null) {
            addressEditText.setText(place.getAddress());
        }
        if (place.getPhoneNumber() != null && phoneEditText != null) {
            phoneEditText.setText(place.getPhoneNumber());
        }
        
        // Update coordinates display
        if (gpsCoordinatesTextView != null) {
            gpsCoordinatesTextView.setText("Place Coordinates: " + 
                    String.format("%.6f, %.6f", latitude, longitude));
            gpsCoordinatesTextView.setTextColor(0xFF2196F3); // Blue
        }
        
        // Hide the search field since we're using map tap
        if (searchEditText != null) {
            searchEditText.setVisibility(View.GONE);
        }

        AlertDialog dialog = builder.setView(dialogView).create();

        addButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(context, "Please enter a location name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate coordinates before creating location
            if (currentLatitude[0] == 0.0 && currentLongitude[0] == 0.0) {
                Toast.makeText(context, "Invalid location coordinates.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create location with contact information
            Location newLocation = new Location(name, currentLatitude[0], currentLongitude[0], 
                    phone, address, email, description);

            repository.insertLocation(newLocation, locations -> {
                ((AppCompatActivity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "Location added: " + name + " at " + 
                            String.format("%.6f, %.6f", currentLatitude[0], currentLongitude[0]), Toast.LENGTH_LONG).show();
                    if (listener != null && locations != null && !locations.isEmpty()) {
                        // Find the newly added location
                        Location insertedLocation = null;
                        for (Location loc : locations) {
                            if (loc.locationName != null && loc.locationName.equals(name) &&
                                Math.abs(loc.latitude - currentLatitude[0]) < 0.0001 &&
                                Math.abs(loc.longitude - currentLongitude[0]) < 0.0001) {
                                insertedLocation = loc;
                                break;
                            }
                        }
                        if (insertedLocation == null && !locations.isEmpty()) {
                            insertedLocation = locations.get(locations.size() - 1);
                        }
                        listener.onLocationAdded(insertedLocation);
                    }
                    dialog.dismiss();
                });
            });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    
    public static void showAtPointWithDetails(Context context, LocationRepository repository, double latitude, double longitude, String placeName, String address, OnLocationAddedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_location_search, null);

        AutoCompleteTextView searchEditText = dialogView.findViewById(R.id.editTextSearchLocation);
        EditText nameEditText = dialogView.findViewById(R.id.editTextLocationName);
        EditText phoneEditText = dialogView.findViewById(R.id.editTextPhone);
        EditText addressEditText = dialogView.findViewById(R.id.editTextAddress);
        EditText emailEditText = dialogView.findViewById(R.id.editTextEmail);
        EditText descriptionEditText = dialogView.findViewById(R.id.editTextDescription);
        EditText latitudeEditText = dialogView.findViewById(R.id.editTextLatitude);
        EditText longitudeEditText = dialogView.findViewById(R.id.editTextLongitude);
        TextView gpsCoordinatesTextView = dialogView.findViewById(R.id.textViewGpsCoordinates);
        Button addButton = dialogView.findViewById(R.id.btnAdd);
        Button cancelButton = dialogView.findViewById(R.id.btnCancel);

        // Use provided coordinates
        final double[] currentLatitude = {latitude};
        final double[] currentLongitude = {longitude};
        final boolean[] locationObtained = {true};
        
        // Set coordinates
        latitudeEditText.setText(String.valueOf(latitude));
        longitudeEditText.setText(String.valueOf(longitude));
        
        // Pre-fill name and address if provided
        if (placeName != null && !placeName.isEmpty() && nameEditText != null) {
            nameEditText.setText(placeName);
        }
        if (address != null && !address.isEmpty() && addressEditText != null) {
            addressEditText.setText(address);
        }
        
        // Update coordinates display
        if (gpsCoordinatesTextView != null) {
            gpsCoordinatesTextView.setText("Coordinates: " + 
                    String.format("%.6f, %.6f", latitude, longitude));
            gpsCoordinatesTextView.setTextColor(0xFF2196F3); // Blue
        }
        
        // Hide the search field since we're using map tap
        if (searchEditText != null) {
            searchEditText.setVisibility(View.GONE);
        }

        AlertDialog dialog = builder.setView(dialogView).create();

        addButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String addressStr = addressEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(context, "Please enter a location name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate coordinates before creating location
            if (currentLatitude[0] == 0.0 && currentLongitude[0] == 0.0) {
                Toast.makeText(context, "Invalid location coordinates.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create location with contact information
            Location newLocation = new Location(name, currentLatitude[0], currentLongitude[0], 
                    phone, addressStr, email, description);

            repository.insertLocation(newLocation, locations -> {
                ((AppCompatActivity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "Location added: " + name + " at " + 
                            String.format("%.6f, %.6f", currentLatitude[0], currentLongitude[0]), Toast.LENGTH_LONG).show();
                    if (listener != null && locations != null && !locations.isEmpty()) {
                        // Find the newly added location
                        Location insertedLocation = null;
                        for (Location loc : locations) {
                            if (loc.locationName != null && loc.locationName.equals(name) &&
                                Math.abs(loc.latitude - currentLatitude[0]) < 0.0001 &&
                                Math.abs(loc.longitude - currentLongitude[0]) < 0.0001) {
                                insertedLocation = loc;
                                break;
                            }
                        }
                        if (insertedLocation == null && !locations.isEmpty()) {
                            insertedLocation = locations.get(locations.size() - 1);
                        }
                        listener.onLocationAdded(insertedLocation);
                    }
                    dialog.dismiss();
                });
            });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    
    public static void showAtPoint(Context context, LocationRepository repository, double latitude, double longitude, OnLocationAddedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_location_search, null);

        AutoCompleteTextView searchEditText = dialogView.findViewById(R.id.editTextSearchLocation);
        EditText nameEditText = dialogView.findViewById(R.id.editTextLocationName);
        EditText phoneEditText = dialogView.findViewById(R.id.editTextPhone);
        EditText addressEditText = dialogView.findViewById(R.id.editTextAddress);
        EditText emailEditText = dialogView.findViewById(R.id.editTextEmail);
        EditText descriptionEditText = dialogView.findViewById(R.id.editTextDescription);
        EditText latitudeEditText = dialogView.findViewById(R.id.editTextLatitude);
        EditText longitudeEditText = dialogView.findViewById(R.id.editTextLongitude);
        TextView gpsCoordinatesTextView = dialogView.findViewById(R.id.textViewGpsCoordinates);
        Button addButton = dialogView.findViewById(R.id.btnAdd);
        Button cancelButton = dialogView.findViewById(R.id.btnCancel);

        // Use provided coordinates (from map tap)
        final double[] currentLatitude = {latitude};
        final double[] currentLongitude = {longitude};
        final boolean[] locationObtained = {true}; // Coordinates are already provided
        
        // Set coordinates
        latitudeEditText.setText(String.valueOf(latitude));
        longitudeEditText.setText(String.valueOf(longitude));
        
        // Update coordinates display
        if (gpsCoordinatesTextView != null) {
            gpsCoordinatesTextView.setText("Coordinates: " + 
                    String.format("%.6f, %.6f", latitude, longitude));
            gpsCoordinatesTextView.setTextColor(0xFF2196F3); // Blue
        }
        
        // Hide the search field since we're using map tap
        if (searchEditText != null) {
            searchEditText.setVisibility(View.GONE);
        }

        // Initialize Places API and get address from coordinates
        if (!Places.isInitialized()) {
            Places.initialize(context, "AIzaSyAmEi2UE7IYVNroxxxI7GMwzbmChfOTQw4");
        }
        PlacesClient placesClient = Places.createClient(context);
        
        // Get address using reverse geocoding
        if (addressEditText != null) {
            addressEditText.setHint("Getting address...");
        }
        
        // Use FindCurrentPlaceRequest with the coordinates to get place details
        // Actually, we need to use Geocoding API or find nearby places
        // Let's use a simpler approach - find places near the coordinates
        com.google.android.gms.maps.model.LatLng latLng = new com.google.android.gms.maps.model.LatLng(latitude, longitude);
        
        // Use Geocoder for reverse geocoding (simpler, built-in)
        // Run in background thread since Geocoder can block
        new Thread(() -> {
            try {
                android.location.Geocoder geocoder = new android.location.Geocoder(context, java.util.Locale.getDefault());
                if (android.location.Geocoder.isPresent()) {
                    java.util.List<android.location.Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null && !addresses.isEmpty() && addressEditText != null) {
                        android.location.Address address = addresses.get(0);
                        StringBuilder addressString = new StringBuilder();
                        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                            if (i > 0) addressString.append(", ");
                            addressString.append(address.getAddressLine(i));
                        }
                        String finalAddress = addressString.toString();
                        // Update UI on main thread
                        ((AppCompatActivity) context).runOnUiThread(() -> {
                            if (addressEditText != null) {
                                addressEditText.setText(finalAddress);
                                addressEditText.setHint("Address");
                            }
                        });
                        return; // Success, don't try Places API
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // If Geocoder fails, try Places API
            getAddressFromPlacesAPI(placesClient, latitude, longitude, addressEditText);
        }).start();

        AlertDialog dialog = builder.setView(dialogView).create();

        addButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(context, "Please enter a location name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate coordinates before creating location
            if (currentLatitude[0] == 0.0 && currentLongitude[0] == 0.0) {
                Toast.makeText(context, "Invalid location coordinates.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create location with contact information
            Location newLocation = new Location(name, currentLatitude[0], currentLongitude[0], 
                    phone, address, email, description);

            repository.insertLocation(newLocation, locations -> {
                ((AppCompatActivity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "Location added: " + name + " at " + 
                            String.format("%.6f, %.6f", currentLatitude[0], currentLongitude[0]), Toast.LENGTH_LONG).show();
                    if (listener != null && locations != null && !locations.isEmpty()) {
                        // Find the newly added location (should be the last one or match by name/coordinates)
                        Location insertedLocation = null;
                        for (Location loc : locations) {
                            if (loc.locationName != null && loc.locationName.equals(name) &&
                                Math.abs(loc.latitude - currentLatitude[0]) < 0.0001 &&
                                Math.abs(loc.longitude - currentLongitude[0]) < 0.0001) {
                                insertedLocation = loc;
                                break;
                            }
                        }
                        // If not found, use the last location (most recently added)
                        if (insertedLocation == null && !locations.isEmpty()) {
                            insertedLocation = locations.get(locations.size() - 1);
                        }
                        listener.onLocationAdded(insertedLocation);
                    }
                    dialog.dismiss();
                });
            });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    
    private static void getAddressFromPlacesAPI(PlacesClient placesClient, double latitude, double longitude, EditText addressEditText) {
        if (placesClient == null || addressEditText == null) return;
        
        // Use FindCurrentPlaceRequest to find places near the coordinates
        // Create a location object using android.location.Location
        android.location.Location location = new android.location.Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        
        // Find current place request
        List<Field> placeFields = Arrays.asList(Field.ID, Field.NAME, Field.ADDRESS);
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);
        
        placesClient.findCurrentPlace(request)
            .addOnSuccessListener((FindCurrentPlaceResponse response) -> {
                if (response != null && !response.getPlaceLikelihoods().isEmpty()) {
                    // Get the most likely place
                    com.google.android.libraries.places.api.model.PlaceLikelihood placeLikelihood = 
                        response.getPlaceLikelihoods().get(0);
                    Place place = placeLikelihood.getPlace();
                    
                    String address = place.getAddress() != null ? place.getAddress() : 
                                    (place.getName() != null ? place.getName() : "");
                    
                    if (!address.isEmpty()) {
                        // Update UI on main thread
                        Context context = addressEditText.getContext();
                        if (context instanceof AppCompatActivity) {
                            ((AppCompatActivity) context).runOnUiThread(() -> {
                                addressEditText.setText(address);
                                addressEditText.setHint("Address");
                            });
                        }
                    }
                }
            })
            .addOnFailureListener(exception -> {
                // If Places API fails, just leave address empty
                Context context = addressEditText.getContext();
                if (context instanceof AppCompatActivity) {
                    ((AppCompatActivity) context).runOnUiThread(() -> {
                        if (addressEditText != null) {
                            addressEditText.setHint("Address (optional)");
                        }
                    });
                }
            });
    }
}
