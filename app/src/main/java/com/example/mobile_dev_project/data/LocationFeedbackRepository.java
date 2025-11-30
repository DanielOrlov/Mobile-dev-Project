package com.example.mobile_dev_project.data;

import android.content.Context;
import com.example.mobile_dev_project.data.local.dao.LocationFeedbackDao;
import com.example.mobile_dev_project.data.local.db.AppDatabase;
import com.example.mobile_dev_project.data.local.entity.LocationFeedback;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class LocationFeedbackRepository {
    private final LocationFeedbackDao feedbackDao;
    private final ExecutorService executorService;

    public LocationFeedbackRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        feedbackDao = db.locationFeedbackDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insertFeedback(LocationFeedback feedback, Consumer<LocationFeedback> callback) {
        executorService.execute(() -> {
            feedbackDao.insert(feedback);
            callback.accept(feedback);
        });
    }

    public void getFeedbacksByLocationId(int locationId, Consumer<List<LocationFeedback>> callback) {
        executorService.execute(() -> {
            List<LocationFeedback> feedbacks = feedbackDao.getFeedbacksByLocationId(locationId);
            callback.accept(feedbacks);
        });
    }

    public void getAverageFeedback(int locationId, Consumer<FeedbackAverages> callback) {
        executorService.execute(() -> {
            Double avgNoise = feedbackDao.getAverageNoiseLevel(locationId);
            Double avgWifi = feedbackDao.getAverageWifiQuality(locationId);
            Double avgBusyness = feedbackDao.getAverageBusyness(locationId);
            Double avgFreeSpace = feedbackDao.getAverageFreeSpace(locationId);
            int something1Count = feedbackDao.getSomething1AvailableCount(locationId);
            int something2Count = feedbackDao.getSomething2AvailableCount(locationId);
            int totalCount = feedbackDao.getFeedbackCount(locationId);

            FeedbackAverages averages = new FeedbackAverages(
                avgNoise != null ? avgNoise : 0.0,
                avgWifi != null ? avgWifi : 0.0,
                avgBusyness != null ? avgBusyness : 0.0,
                avgFreeSpace != null ? avgFreeSpace : 0.0,
                something1Count,
                something2Count,
                totalCount
            );
            callback.accept(averages);
        });
    }

    public static class FeedbackAverages {
        public final double avgNoiseLevel;
        public final double avgWifiQuality;
        public final double avgBusyness;
        public final double avgFreeSpace;
        public final int something1AvailableCount;
        public final int something2AvailableCount;
        public final int totalFeedbackCount;

        public FeedbackAverages(double avgNoiseLevel, double avgWifiQuality, double avgBusyness,
                              double avgFreeSpace, int something1AvailableCount, 
                              int something2AvailableCount, int totalFeedbackCount) {
            this.avgNoiseLevel = avgNoiseLevel;
            this.avgWifiQuality = avgWifiQuality;
            this.avgBusyness = avgBusyness;
            this.avgFreeSpace = avgFreeSpace;
            this.something1AvailableCount = something1AvailableCount;
            this.something2AvailableCount = something2AvailableCount;
            this.totalFeedbackCount = totalFeedbackCount;
        }
    }
}

