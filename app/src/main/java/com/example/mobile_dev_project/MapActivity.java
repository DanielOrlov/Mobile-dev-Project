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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.Place.Field;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;

import com.example.mobile_dev_project.data.LocationRepository;
import com.example.mobile_dev_project.data.local.entity.Location;

import java.util.Arrays;
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
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRepository = new LocationRepository(this);
        
        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(this, "AIzaSyAmEi2UE7IYVNroxxxI7GMwzbmChfOTQw4");
        }
        placesClient = Places.createClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setupButtons();
    }

    private void setupButtons() {
        Button zoomInButton = findViewById(R.id.btnZoomIn);
        Button zoomOutButton = findViewById(R.id.btnZoomOut);

        if (zoomInButton != null) {
            zoomInButton.setOnClickListener(v -> zoomIn());
        }

        if (zoomOutButton != null) {
            zoomOutButton.setOnClickListener(v -> zoomOut());
        }
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
            
            // Add map click listener to find nearby places (only when not clicking on a marker)
            googleMap.setOnMapClickListener(latLng -> {
                // This will be called when clicking on the map (not on a marker)
                // Markers have their own click handler (onMarkerClick) which returns true to consume the event
                findNearbyPlaces(latLng);
            });
            
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
                    if (locations != null && !locations.isEmpty()) {
                        for (Location location : locations) {
                            // Validate coordinates (not 0,0 which is in the ocean)
                            if (location.latitude != 0.0 || location.longitude != 0.0) {
                                LatLng latLng = new LatLng(location.latitude, location.longitude);
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(latLng)
                                        .title(location.locationName);
                                Marker marker = googleMap.addMarker(markerOptions);
                                // Store location ID with marker for later retrieval
                                if (marker != null) {
                                    marker.setTag(location.uid);
                                }
                            }
                        }
                    }
                } else {
                    // Map not ready yet, try again after a short delay
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        loadLocationsAndAddMarkers();
                    }, 500);
                }
            });
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Integer locationId = (Integer) marker.getTag();
        if (locationId != null) {
            showLocationProfileDialog(locationId, marker.getTitle());
        } else {
            Toast.makeText(this, "Clicked: " + marker.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return true; // Return true to consume the event
    }

    private void showLocationProfileDialog(int locationId, String locationName) {
        new AlertDialog.Builder(this)
                .setTitle("Location: " + locationName)
                .setMessage("Would you like to view the location profile?")
                .setPositiveButton("Open Profile", (dialog, which) -> {
                    Intent intent = new Intent(this, LocationProfileActivity.class);
                    intent.putExtra("location_id", locationId);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddLocationDialog() {
        // Get current map center or user location as default
        LatLng defaultLocation = googleMap != null && googleMap.getCameraPosition() != null 
            ? googleMap.getCameraPosition().target 
            : new LatLng(43.6532, -79.3832);
        showAddLocationAtPointDialog(defaultLocation);
    }
    
    private void findNearbyPlaces(LatLng point) {
        Toast.makeText(this, "Finding places...", Toast.LENGTH_SHORT).show();
        
        // First get address from Geocoder, then search Places API with that address
        android.location.Geocoder geocoder = new android.location.Geocoder(this, java.util.Locale.getDefault());
        
        new Thread(() -> {
            String addressString = null;
            try {
                if (android.location.Geocoder.isPresent()) {
                    List<android.location.Address> addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        addressString = addresses.get(0).getAddressLine(0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            final String finalAddress = addressString;
            
            // Now search Places API with the address or common place types
            runOnUiThread(() -> {
                searchPlacesAtCoordinates(point, finalAddress);
            });
        }).start();
    }
    
    private void searchPlacesAtCoordinates(LatLng point, String address) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        
        // Create location bias around the tap point (larger radius - 1km = 0.01 degrees)
        com.google.android.libraries.places.api.model.LocationBias locationBias = 
            com.google.android.libraries.places.api.model.RectangularBounds.newInstance(
                new com.google.android.gms.maps.model.LatLng(point.latitude - 0.01, point.longitude - 0.01),
                new com.google.android.gms.maps.model.LatLng(point.latitude + 0.01, point.longitude + 0.01)
            );
        
        // Try multiple search strategies
        List<String> queries = new java.util.ArrayList<>();
        
        // If we have address, try searching with it
        if (address != null && address.length() > 10) {
            // Extract potential place name from address (first part)
            String[] parts = address.split(",");
            if (parts.length > 0 && parts[0].length() > 3) {
                queries.add(parts[0].trim());
            }
        }
        
        // Always add common place type searches
        queries.add("restaurant");
        queries.add("cafe");
        queries.add("shop");
        queries.add("store");
        queries.add("business");
        
        final List<AutocompletePrediction> allPredictions = new java.util.ArrayList<>();
        final int[] completedSearches = {0};
        final int totalSearches = queries.size();
        
        if (totalSearches == 0) {
            getAddressFromGeocoder(point);
            return;
        }
        
        // Search with each query
        for (String query : queries) {
            FindAutocompletePredictionsRequest request = 
                FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .setLocationBias(locationBias)
                    .setSessionToken(token)
                    .build();
            
            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener((FindAutocompletePredictionsResponse response) -> {
                    if (response != null && !response.getAutocompletePredictions().isEmpty()) {
                        // Add unique predictions
                        for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                            boolean exists = false;
                            for (AutocompletePrediction existing : allPredictions) {
                                if (existing.getPlaceId().equals(prediction.getPlaceId())) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                allPredictions.add(prediction);
                            }
                        }
                    }
                    completedSearches[0]++;
                    if (completedSearches[0] == totalSearches) {
                        if (!allPredictions.isEmpty()) {
                            fetchClosestPlace(allPredictions, point);
                        } else {
                            getAddressFromGeocoder(point);
                        }
                    }
                })
                .addOnFailureListener(exception -> {
                    completedSearches[0]++;
                    if (completedSearches[0] == totalSearches) {
                        if (!allPredictions.isEmpty()) {
                            fetchClosestPlace(allPredictions, point);
                        } else {
                            getAddressFromGeocoder(point);
                        }
                    }
                });
        }
    }
    
    private void fetchClosestPlace(List<AutocompletePrediction> predictions, LatLng point) {
        // Fetch details for predictions and find the closest one
        int count = Math.min(predictions.size(), 10);
        final Place[] places = new Place[count];
        final String[] placeNames = new String[count];
        final double[] distances = new double[count];
        final int[] fetchedCount = {0};
        
        List<Field> placeFields = Arrays.asList(Field.ID, Field.NAME, Field.ADDRESS, Field.LAT_LNG, Field.PHONE_NUMBER);
        
        for (int i = 0; i < count; i++) {
            AutocompletePrediction prediction = predictions.get(i);
            String placeId = prediction.getPlaceId();
            FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);
            
            final int index = i;
            placesClient.fetchPlace(request)
                .addOnSuccessListener((FetchPlaceResponse response) -> {
                    if (response != null && response.getPlace() != null) {
                        Place place = response.getPlace();
                        places[index] = place;
                        
                        // Calculate distance
                        if (place.getLatLng() != null) {
                            distances[index] = calculateDistance(
                                point.latitude, point.longitude,
                                place.getLatLng().latitude, place.getLatLng().longitude
                            );
                        } else {
                            distances[index] = Double.MAX_VALUE;
                        }
                        
                        String name = place.getName() != null ? place.getName() : "Unknown";
                        String address = place.getAddress() != null ? " - " + place.getAddress() : "";
                        String distanceStr = distances[index] < 1000 ? 
                            String.format(" (%.0fm)", distances[index]) : 
                            String.format(" (%.1fkm)", distances[index] / 1000);
                        placeNames[index] = name + address + distanceStr;
                    }
                    fetchedCount[0]++;
                    if (fetchedCount[0] == count) {
                        // Find the closest place
                        int closestIndex = -1;
                        double minDistance = Double.MAX_VALUE;
                        for (int j = 0; j < count; j++) {
                            if (places[j] != null && distances[j] < minDistance) {
                                minDistance = distances[j];
                                closestIndex = j;
                            }
                        }
                        
                        if (closestIndex >= 0 && places[closestIndex] != null) {
                            if (minDistance < 50) { // Within 50m - use directly
                                showAddLocationAtPlace(places[closestIndex], point);
                            } else {
                                // Show selection dialog with all found places (up to 1km)
                                filterAndShowNearbyPlaces(places, placeNames, distances, point, 1000.0);
                            }
                        } else {
                            // No valid places found - try a broader search or fallback
                            getAddressFromGeocoder(point);
                        }
                    }
                })
                .addOnFailureListener(exception -> {
                    fetchedCount[0]++;
                    if (fetchedCount[0] == count) {
                        // Check if we got any places
                        int closestIndex = -1;
                        double minDistance = Double.MAX_VALUE;
                        for (int j = 0; j < count; j++) {
                            if (places[j] != null && distances[j] < minDistance) {
                                minDistance = distances[j];
                                closestIndex = j;
                            }
                        }
                        if (closestIndex >= 0 && places[closestIndex] != null) {
                            // Show selection dialog with all found places (up to 1km)
                            filterAndShowNearbyPlaces(places, placeNames, distances, point, 1000.0);
                        } else {
                            getAddressFromGeocoder(point);
                        }
                    }
                });
        }
    }
    
    private void getAddressFromGeocoder(LatLng point) {
        // Use Geocoder to get address as fallback
        android.location.Geocoder geocoder = new android.location.Geocoder(this, java.util.Locale.getDefault());
        
        new Thread(() -> {
            try {
                if (android.location.Geocoder.isPresent()) {
                    List<android.location.Address> addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        android.location.Address address = addresses.get(0);
                        String addressString = address.getAddressLine(0);
                        
                        // Try to extract a place name from address components
                        // DO NOT use sub-locality (like "Old Toronto") - it's too generic
                        String placeName = null;
                        // Try feature name first (actual place name)
                        if (address.getFeatureName() != null && !address.getFeatureName().isEmpty() && !isNumeric(address.getFeatureName())) {
                            placeName = address.getFeatureName();
                        }
                        // Try premises (building name)
                        else if (address.getPremises() != null && !address.getPremises().isEmpty() && !isNumeric(address.getPremises())) {
                            placeName = address.getPremises();
                        }
                        // DO NOT use sub-locality - it's too generic (e.g., "Old Toronto")
                        // Leave placeName as null so user enters it manually
                        
                        final String finalPlaceName = placeName;
                        
                        runOnUiThread(() -> {
                            // Show dialog with address and extracted name (if available)
                            showAddLocationWithGeocodedAddress(point, addressString, finalPlaceName);
                        });
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Could not get address. Please enter location details manually.", Toast.LENGTH_LONG).show();
            });
        }).start();
    }
    
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private void searchPlacesByAddress(LatLng point, String addressString, String placeName, String featureName, String thoroughfare) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        
        // Create location bias around the tap point (smaller radius for accuracy)
        com.google.android.libraries.places.api.model.LocationBias locationBias = 
            com.google.android.libraries.places.api.model.RectangularBounds.newInstance(
                new com.google.android.gms.maps.model.LatLng(point.latitude - 0.02, point.longitude - 0.02),
                new com.google.android.gms.maps.model.LatLng(point.latitude + 0.02, point.longitude + 0.02)
            );
        
        // Build search queries from the address components
        List<String> queries = new java.util.ArrayList<>();
        
        // If we have a feature name (building/place name), search for it
        if (featureName != null && !featureName.isEmpty() && featureName.length() > 2) {
            queries.add(featureName);
        }
        
        // If we have a street name, search for places on that street
        if (thoroughfare != null && !thoroughfare.isEmpty()) {
            queries.add(thoroughfare);
        }
        
        // Always add generic place searches
        queries.add("restaurant");
        queries.add("cafe");
        queries.add("shop");
        queries.add("store");
        
        // If we have full address, try searching for it
        if (addressString != null && addressString.length() > 10) {
            // Extract potential place name from address (first part before comma)
            String[] parts = addressString.split(",");
            if (parts.length > 0 && parts[0].length() > 3) {
                queries.add(parts[0].trim());
            }
        }
        
        final List<AutocompletePrediction> allPredictions = new java.util.ArrayList<>();
        final int[] completedSearches = {0};
        final int totalSearches = queries.size();
        
        if (totalSearches == 0) {
            // No queries to search, just show dialog with geocoded address
            showAddLocationWithGeocodedAddress(point, addressString, placeName);
            return;
        }
        
        // Search with each query
        for (String query : queries) {
            FindAutocompletePredictionsRequest request = 
                FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .setLocationBias(locationBias)
                    .setSessionToken(token)
                    .build();
            
            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener((FindAutocompletePredictionsResponse response) -> {
                    if (response != null && !response.getAutocompletePredictions().isEmpty()) {
                        // Add unique predictions (by place ID)
                        for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                            boolean exists = false;
                            for (AutocompletePrediction existing : allPredictions) {
                                if (existing.getPlaceId().equals(prediction.getPlaceId())) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                allPredictions.add(prediction);
                            }
                        }
                    }
                    completedSearches[0]++;
                    if (completedSearches[0] == totalSearches) {
                        // All searches completed
                        if (!allPredictions.isEmpty()) {
                            fetchPlacesFromPredictions(allPredictions, point);
                        } else {
                            // No places found via API, but we have address - show dialog with name
                            if (addressString != null) {
                                showAddLocationWithGeocodedAddress(point, addressString, placeName);
                            } else {
                                showAddLocationWithGeocodedAddress(point);
                            }
                        }
                    }
                })
                .addOnFailureListener(exception -> {
                    completedSearches[0]++;
                    if (completedSearches[0] == totalSearches) {
                        if (!allPredictions.isEmpty()) {
                            fetchPlacesFromPredictions(allPredictions, point);
                        } else {
                            if (addressString != null) {
                                showAddLocationWithGeocodedAddress(point, addressString, placeName);
                            } else {
                                showAddLocationWithGeocodedAddress(point);
                            }
                        }
                    }
                });
        }
    }
    
    private void fetchPlacesFromPredictions(List<AutocompletePrediction> predictions, LatLng point) {
        // Limit to first 10 predictions and filter by distance
        int count = Math.min(predictions.size(), 10);
        final Place[] places = new Place[count];
        final String[] placeNames = new String[count];
        final double[] distances = new double[count];
        final int[] fetchedCount = {0};
        
        List<Field> placeFields = Arrays.asList(Field.ID, Field.NAME, Field.ADDRESS, Field.LAT_LNG, Field.PHONE_NUMBER);
        
        for (int i = 0; i < count; i++) {
            AutocompletePrediction prediction = predictions.get(i);
            String placeId = prediction.getPlaceId();
            FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);
            
            final int index = i;
            placesClient.fetchPlace(request)
                .addOnSuccessListener((FetchPlaceResponse response) -> {
                    if (response != null && response.getPlace() != null) {
                        Place place = response.getPlace();
                        places[index] = place;
                        
                        // Calculate distance from tap point
                        if (place.getLatLng() != null) {
                            double distance = calculateDistance(
                                point.latitude, point.longitude,
                                place.getLatLng().latitude, place.getLatLng().longitude
                            );
                            distances[index] = distance;
                        } else {
                            distances[index] = Double.MAX_VALUE; // Place without coordinates
                        }
                        
                        String name = place.getName() != null ? place.getName() : "Unknown";
                        String address = place.getAddress() != null ? " - " + place.getAddress() : "";
                        String distanceStr = distances[index] < 1000 ? 
                            String.format(" (%.0fm away)", distances[index]) : 
                            String.format(" (%.1fkm away)", distances[index] / 1000);
                        placeNames[index] = name + address + distanceStr;
                    }
                    fetchedCount[0]++;
                    if (fetchedCount[0] == count) {
                        // Filter places within 1km and show dialog
                        filterAndShowNearbyPlaces(places, placeNames, distances, point, 1000.0);
                    }
                })
                .addOnFailureListener(exception -> {
                    fetchedCount[0]++;
                    if (fetchedCount[0] == count) {
                        filterAndShowNearbyPlaces(places, placeNames, distances, point, 1000.0);
                    }
                });
        }
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula to calculate distance in meters
        final int R = 6371000; // Earth radius in meters
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    private void filterAndShowNearbyPlaces(Place[] places, String[] placeNames, double[] distances, LatLng point, double maxDistance) {
        // Filter places within maxDistance (increased to 1km for better results)
        List<Place> nearbyPlaces = new java.util.ArrayList<>();
        List<String> nearbyNames = new java.util.ArrayList<>();
        
        for (int i = 0; i < places.length; i++) {
            if (places[i] != null && distances[i] <= maxDistance) {
                nearbyPlaces.add(places[i]);
                nearbyNames.add(placeNames[i] != null ? placeNames[i] : "Unknown Place");
            }
        }
        
        if (nearbyPlaces.isEmpty()) {
            // No places within range - show all places found (up to 10 closest) as fallback
            List<Place> allValidPlaces = new java.util.ArrayList<>();
            List<String> allValidNames = new java.util.ArrayList<>();
            List<Double> allDistances = new java.util.ArrayList<>();
            
            for (int i = 0; i < places.length; i++) {
                if (places[i] != null) {
                    allValidPlaces.add(places[i]);
                    allValidNames.add(placeNames[i] != null ? placeNames[i] : "Unknown Place");
                    allDistances.add(distances[i]);
                }
            }
            
            if (allValidPlaces.isEmpty()) {
                // No places found at all - try to get address from geocoder
                getAddressFromGeocoder(point);
                return;
            }
            
            // Sort by distance and take closest 10
            // Create a list of indices sorted by distance
            List<Integer> indices = new java.util.ArrayList<>();
            for (int i = 0; i < allValidPlaces.size(); i++) {
                indices.add(i);
            }
            indices.sort((i1, i2) -> Double.compare(allDistances.get(i1), allDistances.get(i2)));
            
            List<Place> sortedPlaces = new java.util.ArrayList<>();
            List<String> sortedNames = new java.util.ArrayList<>();
            
            int count = Math.min(10, indices.size());
            for (int i = 0; i < count; i++) {
                int idx = indices.get(i);
                sortedPlaces.add(allValidPlaces.get(idx));
                sortedNames.add(allValidNames.get(idx));
            }
            
            final Place[] finalPlaces = sortedPlaces.toArray(new Place[0]);
            String[] finalNames = sortedNames.toArray(new String[0]);
            
            new AlertDialog.Builder(this)
                    .setTitle("Select a Place")
                    .setItems(finalNames, (dialog, which) -> {
                        Place selectedPlace = finalPlaces[which];
                        showAddLocationAtPlace(selectedPlace, point);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }
        
        // Sort by distance
        java.util.Map<Place, String> placeMap = new java.util.HashMap<>();
        java.util.Map<Place, Double> distanceMap = new java.util.HashMap<>();
        for (int i = 0; i < nearbyPlaces.size(); i++) {
            placeMap.put(nearbyPlaces.get(i), nearbyNames.get(i));
            distanceMap.put(nearbyPlaces.get(i), distances[i]);
        }
        
        nearbyPlaces.sort((p1, p2) -> Double.compare(distanceMap.get(p1), distanceMap.get(p2)));
        nearbyNames.clear();
        for (Place place : nearbyPlaces) {
            nearbyNames.add(placeMap.get(place));
        }
        
        final Place[] finalPlaces = nearbyPlaces.toArray(new Place[0]);
        String[] finalNames = nearbyNames.toArray(new String[0]);
        
        new AlertDialog.Builder(this)
                .setTitle("Select a Place (within 1km)")
                .setItems(finalNames, (dialog, which) -> {
                    Place selectedPlace = finalPlaces[which];
                    showAddLocationAtPlace(selectedPlace, point);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showPlacesSelectionDialog(Place[] places, String[] placeNames, LatLng point) {
        // Filter out null places
        List<Place> validPlaces = new java.util.ArrayList<>();
        List<String> validNames = new java.util.ArrayList<>();
        
        for (int i = 0; i < places.length; i++) {
            if (places[i] != null) {
                validPlaces.add(places[i]);
                validNames.add(placeNames[i] != null ? placeNames[i] : "Unknown Place");
            }
        }
        
        if (validPlaces.isEmpty()) {
            // No places found, use geocoded address
            showAddLocationWithGeocodedAddress(point);
            return;
        }
        
        final Place[] finalPlaces = validPlaces.toArray(new Place[0]);
        String[] finalNames = validNames.toArray(new String[0]);
        
        new AlertDialog.Builder(this)
                .setTitle("Select a Place")
                .setItems(finalNames, (dialog, which) -> {
                    Place selectedPlace = finalPlaces[which];
                    showAddLocationAtPlace(selectedPlace, point);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showAddLocationWithGeocodedAddress(LatLng point) {
        showAddLocationWithGeocodedAddress(point, null, null);
    }
    
    private void showAddLocationWithGeocodedAddress(LatLng point, String address, String placeName) {
        // If we have an address, allow adding the location
        if (address != null && !address.isEmpty()) {
            // Show dialog with address and name pre-filled
            AddLocationDialog.showAtPointWithDetails(this, locationRepository, point.latitude, point.longitude, placeName, address, newLocation -> {
                if (googleMap != null) {
                    loadLocationsAndAddMarkers();
                    if (newLocation != null) {
                        LatLng newLocationLatLng = new LatLng(newLocation.latitude, newLocation.longitude);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocationLatLng, 15));
                    }
                }
            });
        } else {
            Toast.makeText(this, "Could not get address for this location. Please try tapping on a marked place or a known location.", Toast.LENGTH_LONG).show();
        }
    }
    
    private void showAddLocationAtPlace(Place place, LatLng point) {
        // Get coordinates from place if available, otherwise use tap point
        double latitude = point.latitude;
        double longitude = point.longitude;
        if (place.getLatLng() != null) {
            latitude = place.getLatLng().latitude;
            longitude = place.getLatLng().longitude;
        }
        
        AddLocationDialog.showAtPlace(this, locationRepository, place, latitude, longitude, newLocation -> {
            // Refresh the map after adding location
            if (googleMap != null) {
                loadLocationsAndAddMarkers();
                // Move camera to the new location
                if (newLocation != null) {
                    LatLng newLocationLatLng = new LatLng(newLocation.latitude, newLocation.longitude);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocationLatLng, 15));
                }
            } else {
                // If map is not ready, wait a bit and try again
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    loadLocationsAndAddMarkers();
                }, 500);
            }
        });
    }
    
    private void showAddLocationAtPointDialog(LatLng point) {
        AddLocationDialog.showAtPoint(this, locationRepository, point.latitude, point.longitude, newLocation -> {
            // Refresh the map after adding location
            if (googleMap != null) {
                loadLocationsAndAddMarkers();
                // Move camera to the new location
                if (newLocation != null) {
                    LatLng newLocationLatLng = new LatLng(newLocation.latitude, newLocation.longitude);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocationLatLng, 15));
                }
            } else {
                // If map is not ready, wait a bit and try again
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    loadLocationsAndAddMarkers();
                }, 500);
            }
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