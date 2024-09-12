package com.ass1.data;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataLoader{
    public static List<City> loadCityData(String filePath) {
        List<City> cities = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); 

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(";");

                if (fields.length >= 7) {

                    int geonameId = Integer.parseInt(fields[0].trim());
                    String name = fields[1].trim();
                    String countryCode = fields[2].trim();
                    String countryName = fields[3].trim();
                    int population = Integer.parseInt(fields[4].trim());
                    String timezone = fields[5].trim();
                    String coordinates = fields[6].trim();

                    City city = new City(geonameId, name, countryCode, countryName, population, timezone, coordinates);
                    cities.add(city);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cities;
    }
}
