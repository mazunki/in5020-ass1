package com.ass1.data;
import java.util.HashMap;

public class TestDataLoader {
    public static void main(String[] args) {
        testLoadCityData("src/main/resources/exercise_1_dataset.csv", 5);
    }

    public static void testLoadCityData(String filePath, int limit) {
        try {
            HashMap<Integer, Geoname> data = GeonameLoader.loadCityData(filePath, 5);


            for (Geoname geoname : data.values()) {
                    System.out.println(geoname);
            }
            
        } catch (IllegalArgumentException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }
}
