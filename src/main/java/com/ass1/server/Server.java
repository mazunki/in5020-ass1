package com.ass1.server;

import java.util.List;

import com.ass1.data.Geoname;
import com.ass1.data.GeonameLoader;

public class Server implements ServerInterfaceImpl {
	public Integer getPopulationOfCountry(String[] args) {
		return this.getPopulationOfCountry(args[0]);
	}

	public Integer getPopulationOfCountry(String countryName) {
		int population = 0;

		List<Geoname> cities = GeonameLoader.getByName(countryName);

		for (Geoname city : cities) {
			population += city.getPopulation();
		}

		return population;
	}

	public Integer getNumberOfCities(String[] args) {
		return this.getNumberOfCities(args[0], Integer.parseInt(args[1]));
	}

	public Integer getNumberOfCities(String countryName, int minPopulation) {
		int cityCount = 0;

		List<Geoname> cities = GeonameLoader.getByName(countryName);

		for (Geoname city : cities) {
			if (city.getPopulation() >= minPopulation) {
				cityCount++;
			}
		}

		return cityCount;
	}

	public Integer getNumberOfCountries(String[] args) {
		if (args.length == 3) {
			return this.getNumberOfCountries(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
					Integer.parseInt(args[2]));
		}
		return this.getNumberOfCountries(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
	}

	public Integer getNumberOfCountries(int cityCount, int minPopulation) {
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

	public Integer getNumberOfCountries(int cityCount, int minPopulation, int maxPopulation) {
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
