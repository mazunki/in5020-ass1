package com.ass1.data;

public class Geoname {
	int id;
	String name;
	String countryCode;
	String countryName;
	int population;
	String timezone;
	Coordinate coordinates;


	public Geoname(int id, String name, String countryCode, String countryName, int population, 
	String timezone,Coordinate coordinates) {
		this.id = id;
		this.name = name;
		this.countryCode = countryCode;
		this.countryName = countryName;
		this.population = population;
		this.timezone = timezone;
		this.coordinates = coordinates;
	}

	public static Geoname fromCSVEntry(String[] fields) throws IllegalArgumentException {
		if (fields.length < 7) {
			throw new IllegalArgumentException("Geonames requires 7 fields. Got " + fields.length);
		}

		int geonameId = Integer.parseInt(fields[0]);
		String name = fields[1];
		String countryCode = fields[2];
		String countryName = fields[3];
		int population = Integer.parseInt(fields[4]);
		String timezone = fields[5];
		Coordinate coordinates = new Coordinate(fields[6]);

		return new Geoname(geonameId, name, countryCode, countryName, population, timezone, coordinates);

	}

	public String toString() {
        return "Geoname<"
                + "id=" + this.id + ","
                + "name='" + name + "', "
                + "countryCode='" + countryCode + "', "
                + "countryName='" + countryName + "', "
                + "population=" + population + ", "
                + "timezone='" + timezone + "', "
                + "coordinates=" + coordinates
                + ">";
    }
}
