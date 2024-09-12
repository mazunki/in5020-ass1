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

	public static void main(String[] args) {
		Geoname sample = new Geoname(2798301, "Fleron", "BE", "Belgium", 15994, "Europe/Brussels", 50.61516,
				5.68062);
		long objectSize = ObjectSize.getObjectSize(sample);
		int totalCities = 140574;
		long totalMemoryUsage = objectSize * totalCities;

		System.out.println("One city: " + objectSize + " bytes");
		System.out.println("All cities: " + totalMemoryUsage/1024 + "kb");
	}
}
