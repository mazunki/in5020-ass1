package com.ass1.server;

public interface ServerInterfaceImpl {
	Integer getPopulationOfCountry(String[] args);

	Integer getPopulationOfCountry(String countryName);

	Integer getNumberOfCities(String[] args);

	Integer getNumberOfCities(String countryName, int minPopulation);

	Integer getNumberOfCountries(String[] args);

	Integer getNumberOfCountries(int cityCount, int minPopulation);

	Integer getNumberOfCountries(int cityCount, int minPopulation, int maxPopulation);

}
