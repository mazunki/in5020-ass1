package com.ass1.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class GeonameLoader {
    public static HashMap<Integer, Geoname> loadCityData(String filePath) throws IllegalArgumentException {
        return loadCityData(filePath, null);
    }
    public static HashMap<Integer, Geoname>loadCityData(String filePath, Integer limit) throws IllegalArgumentException {
        HashMap<Integer, Geoname> entries = new HashMap<Integer, Geoname>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] fields;
            br.readLine(); // our csv file has a header (skip)

            while ((line = br.readLine()) != null) {
                if (limit != null && entries.size() >= limit) {
                    break;
                }

                fields = line.split(";");

                try {
                    Geoname g = Geoname.fromCSVEntry(fields);
                    entries.put(g.id, g);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid data on line: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entries;
    }
}



