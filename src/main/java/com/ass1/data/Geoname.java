package com.ass1.data;

public class Geoname {
	int id;
	String name;
	String countryCode;
	String countryName;
	int population;
	String timezone;
	double latitude;
	double longitude;

	public Geoname(int id, String name, String countryCode, String countryName, int population, String timezone,
			double latitute, double longitute) {
		this.id = id;
		this.name = name;
		this.countryCode = countryCode;
		this.countryName = countryName;
		this.population = population;
		this.timezone = timezone;
		this.latitude = latitude;
		this.longitude = longitude;

	}

}
