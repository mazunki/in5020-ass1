package com.ass1.server;

import java.rmi.RemoteException;

public class TestServer {
    public static void main(String[] args) {
        try {
            // Create the server instance
            Server server = new Server("test");

            // Test 1: getPopulationofCountry
            System.out.println("Test 1: getPopulationofCountry");
            String countryName = "Norway";
            int population = server.getPopulationofCountry(countryName);
            int expectedPopulation = 3162856; // Expected population of Norway
            System.out.println("Country: " + countryName + ", Expected Population: " + expectedPopulation);
            System.out.println("Actual Population: " + population);
            System.out.println("Test Passed: " + (population == expectedPopulation));

            // Test 2: getNumberofCities (Norway, population >= 100,000)
            System.out.println("\nTest 2: getNumberofCities");
            countryName = "Norway";
            int minPopulation = 100000;
            int numberOfCities = server.getNumberofCities(countryName, minPopulation);
            int expectedNumberOfCities = 4; // Norway should have 4 cities with population >= 100,000
            System.out.println("Country: " + countryName + ", Min Population: " + minPopulation);
            System.out.println("Expected Number of Cities: " + expectedNumberOfCities);
            System.out.println("Actual Number of Cities: " + numberOfCities);
            System.out.println("Test Passed: " + (numberOfCities == expectedNumberOfCities));

            // Test 3: getNumberofCountries (min city count = 2, min population = 5,000,000)
            System.out.println("\nTest 3: getNumberofCountries (cityCount = 2, minPopulation = 5,000,000)");
            int cityCount = 2;
            minPopulation = 5000000;
            int numberOfCountries = server.getNumberofCountries(cityCount, minPopulation);
            int expectedNumberOfCountries = 7; // Expecting 7 countries with at least 2 cities having population >= 5,000,000
            System.out.println("Expected Number of Countries: " + expectedNumberOfCountries);
            System.out.println("Actual Number of Countries: " + numberOfCountries);
            System.out.println("Test Passed: " + (numberOfCountries == expectedNumberOfCountries));

            // Test 4: getNumberofCountries (cityCount = 30, population between 100,000 and 800,000)
            System.out.println("\nTest 4: getNumberofCountries (cityCount = 30, minPopulation = 100,000, maxPopulation = 800,000)");
            cityCount = 30;
            minPopulation = 100000;
            int maxPopulation = 800000;
            numberOfCountries = server.getNumberofCountries(cityCount, minPopulation, maxPopulation);
            expectedNumberOfCountries = 30; // Expecting 30 countries with at least 30 cities having population between 100,000 and 800,000
            System.out.println("Expected Number of Countries: " + expectedNumberOfCountries);
            System.out.println("Actual Number of Countries: " + numberOfCountries);
            System.out.println("Test Passed: " + (numberOfCountries == expectedNumberOfCountries));

        } catch (RemoteException e) {
            e.printStackTrace();
}
    }
}
