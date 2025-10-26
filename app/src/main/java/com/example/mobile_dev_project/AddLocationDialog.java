package com.example.mobile_dev_project;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import com.example.mobile_dev_project.data.LocationRepository;
import com.example.mobile_dev_project.data.local.entity.Location;

import java.util.Arrays;
import java.util.List;

public class AddLocationDialog {

    public interface OnLocationAddedListener {
        void onLocationAdded();
    }

    public static void show(Context context, LocationRepository repository, OnLocationAddedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_location_search, null);

        AutoCompleteTextView searchEditText = dialogView.findViewById(R.id.editTextSearchLocation);
        EditText nameEditText = dialogView.findViewById(R.id.editTextLocationName);
        EditText latitudeEditText = dialogView.findViewById(R.id.editTextLatitude);
        EditText longitudeEditText = dialogView.findViewById(R.id.editTextLongitude);
        Button addButton = dialogView.findViewById(R.id.btnAdd);
        Button cancelButton = dialogView.findViewById(R.id.btnCancel);

        if (!Places.isInitialized()) {
            Places.initialize(context, "AIzaSyAmEi2UE7IYVNroxxxI7GMwzbmChfOTQw4");
        }
        PlacesClient placesClient = Places.createClient(context);
        String[] commonPlaces = {
            "Starbucks", "Tim Hortons", "McDonald's", "Subway", "Pizza Pizza",
            "Second Cup", "A&W", "Burger King", "KFC", "Wendy's",
            "Coffee Time", "Dairy Queen", "Harvey's", "Swiss Chalet", "Boston Pizza"
        };
        
        android.widget.ArrayAdapter<String> simpleAdapter = new android.widget.ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, commonPlaces);
        searchEditText.setAdapter(simpleAdapter);
        searchEditText.setThreshold(1);

        searchEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedPlace = simpleAdapter.getItem(position);
                if (selectedPlace != null) {
                    nameEditText.setText(selectedPlace);
                    
                    double lat, lng;
                    switch (selectedPlace.toLowerCase()) {
                        case "starbucks":
                            lat = 43.6478; lng = -79.3754; break;
                        case "tim hortons":
                            lat = 43.6627; lng = -79.3957; break;
                        case "mcdonald's":
                            lat = 43.6426; lng = -79.3871; break;
                        case "subway":
                            lat = 43.6509; lng = -79.3602; break;
                        case "pizza pizza":
                            lat = 43.6677; lng = -79.3948; break;
                        case "second cup":
                            lat = 43.6256; lng = -79.3847; break;
                        case "a&w":
                            lat = 43.6780; lng = -79.4095; break;
                        case "burger king":
                            lat = 43.6532; lng = -79.3832; break;
                        case "kfc":
                            lat = 43.6408; lng = -79.4006; break;
                        case "wendy's":
                            lat = 43.6555; lng = -79.3622; break;
                        case "coffee time":
                            lat = 43.6619; lng = -79.3701; break;
                        case "dairy queen":
                            lat = 43.6389; lng = -79.3755; break;
                        case "harvey's":
                            lat = 43.6702; lng = -79.3866; break;
                        case "swiss chalet":
                            lat = 43.6451; lng = -79.3954; break;
                        case "boston pizza":
                            lat = 43.6321; lng = -79.3923; break;
                        default:
                            lat = 43.6532; lng = -79.3832; break;
                    }
                    
                    latitudeEditText.setText(String.valueOf(lat));
                    longitudeEditText.setText(String.valueOf(lng));
                    Toast.makeText(context, "Selected: " + selectedPlace + 
                        " at " + lat + ", " + lng, Toast.LENGTH_LONG).show();
                }
            }
        });

        AlertDialog dialog = builder.setView(dialogView).create();

        addButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String latStr = latitudeEditText.getText().toString().trim();
            String lonStr = longitudeEditText.getText().toString().trim();

            if (name.isEmpty() || latStr.isEmpty() || lonStr.isEmpty()) {
                Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double latitude = Double.parseDouble(latStr);
                double longitude = Double.parseDouble(lonStr);

                Location newLocation = new Location(name, latitude, longitude);

                repository.insertLocation(newLocation, locations -> {
                    ((AppCompatActivity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Location added: " + name, Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onLocationAdded();
                        }
                        dialog.dismiss();
                    });
                });

            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid latitude or longitude", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
