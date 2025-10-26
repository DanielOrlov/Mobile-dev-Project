package com.example.mobile_dev_project.data;

import android.content.Context;
import com.example.mobile_dev_project.data.local.dao.LocationDao;
import com.example.mobile_dev_project.data.local.db.AppDatabase;
import com.example.mobile_dev_project.data.local.entity.Location;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class LocationRepository {
    private final LocationDao locationDao;
    private final ExecutorService executorService;

    public LocationRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        locationDao = db.locationDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insertLocation(Location location, Consumer<List<Location>> callback) {
        executorService.execute(() -> {
            locationDao.insert(location);
            getAllLocations(callback);
        });
    }

    public void getAllLocations(Consumer<List<Location>> callback) {
        executorService.execute(() -> {
            List<Location> locations = locationDao.getAll();
            callback.accept(locations);
        });
    }

    public void insertAllLocations(Location[] locations, Consumer<List<Location>> callback) {
        executorService.execute(() -> {
            locationDao.insertAll(locations);
            getAllLocations(callback);
        });
    }
}
