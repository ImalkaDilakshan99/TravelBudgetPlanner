// Step 2: Create a storage service class

package com.example.travelbudgetplanner.service;

import com.example.travelbudgetplanner.model.TripData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class to handle saving and loading trip data to/from local storage
 */
public class TripDataStorageService {

    private static final String DATA_DIRECTORY = "TravelBudgetData";
    private static final String TRIPS_FILE = "trips.json";
    private static final String CURRENT_TRIP_FILE = "current_trip.json";

    private final ObjectMapper objectMapper;
    private final String dataPath;

    public TripDataStorageService() {
        // Initialize JSON mapper with Java 8 time support
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Create data directory in user's home directory
        String userHome = System.getProperty("user.home");
        this.dataPath = Paths.get(userHome, DATA_DIRECTORY).toString();
        createDataDirectory();
    }

    /**
     * Create data directory if it doesn't exist
     */
    private void createDataDirectory() {
        File directory = new File(dataPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Save current trip data
     */
    public void saveCurrentTrip(TripData tripData) throws IOException {
        File file = new File(dataPath, CURRENT_TRIP_FILE);
        objectMapper.writeValue(file, tripData);
    }

    /**
     * Load current trip data
     */
    public TripData loadCurrentTrip() throws IOException {
        File file = new File(dataPath, CURRENT_TRIP_FILE);
        if (!file.exists()) {
            return new TripData(); // Return empty trip data if file doesn't exist
        }
        return objectMapper.readValue(file, TripData.class);
    }

    /**
     * Save a trip to the trips history
     */
    public void saveTripToHistory(TripData tripData) throws IOException {
        List<TripData> trips = loadAllTrips();

        // Check if trip already exists (by destination and dates)
        boolean exists = trips.stream().anyMatch(t ->
                t.getDestination().equals(tripData.getDestination()) &&
                        t.getStartDate().equals(tripData.getStartDate()) &&
                        t.getEndDate().equals(tripData.getEndDate())
        );

        if (!exists) {
            trips.add(tripData);
            saveAllTrips(trips);
        }
    }

    /**
     * Load all saved trips
     */
    public List<TripData> loadAllTrips() throws IOException {
        File file = new File(dataPath, TRIPS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(file,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, TripData.class));
        } catch (IOException e) {
            // If file is corrupted, return empty list
            return new ArrayList<>();
        }
    }

    /**
     * Save all trips
     */
    private void saveAllTrips(List<TripData> trips) throws IOException {
        File file = new File(dataPath, TRIPS_FILE);
        objectMapper.writeValue(file, trips);
    }

    /**
     * Delete current trip data
     */
    public void clearCurrentTrip() {
        File file = new File(dataPath, CURRENT_TRIP_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Get the data directory path
     */
    public String getDataPath() {
        return dataPath;
    }

    /**
     * Check if current trip data exists
     */
    public boolean currentTripExists() {
        File file = new File(dataPath, CURRENT_TRIP_FILE);
        return file.exists();
    }
}