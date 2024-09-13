package com.ass1.server;

import com.ass1.data.Geoname;
import com.ass1.data.GeonameLoader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class Server extends UnicastRemoteObject implements ServerInterface {

	public Server() throws RemoteException {
		super();
	}

	    public int getPopulationofCountry(String countryName) throws RemoteException {
			int population = 0;

			// Use the GeonameLoader to fetch cities for the given country name
			List<Geoname> cities = GeonameLoader.getByName(countryName);

			for (Geoname city : cities) {
				population += city.getPopulation();
			}

			return population;
    }

	public int getNumberofCities(String countryName, int minPopulation) throws RemoteException {
        int cityCount = 0;

        // Use the GeonameLoader to fetch cities for the given country name
        List<Geoname> cities = GeonameLoader.getByName(countryName);

        for (Geoname city : cities) {
            if (city.getPopulation() >= minPopulation) {
                cityCount++;
            }
        }

        return cityCount;
    }

	public int getNumberofCountries(int cityCount, int minPopulation) throws RemoteException {
        List<String> countryNames = GeonameLoader.getAllCountryNames(); // Get all country names
        int matchingCountryCount = 0;

        for (String countryName : countryNames) {
            int matchingCityCount = 0;

            List<Geoname> cities = GeonameLoader.getByName(countryName);

            for (Geoname city : cities) {
                if (city.getPopulation() >= minPopulation) {
                    matchingCityCount++;
                }

                if (matchingCityCount >= cityCount) {
                    matchingCountryCount++;
                    break;
                }
            }
        }

        return matchingCountryCount;
    }

	public int getNumberofCountries(int cityCount, int minPopulation, int maxPopulation) throws RemoteException {
        List<String> countryNames = GeonameLoader.getAllCountryNames(); // Get all country names
        int matchingCountryCount = 0;

        for (String countryName : countryNames) {
            int matchingCityCount = 0;

            List<Geoname> cities = GeonameLoader.getByName(countryName);

            for (Geoname city : cities) {
                if (city.getPopulation() >= minPopulation && city.getPopulation() <= maxPopulation) {
                    matchingCityCount++;
                }

                if (matchingCityCount >= cityCount) {
                    matchingCountryCount++;
                    break;
                }
            }
        }

        return matchingCountryCount;
    }
}



