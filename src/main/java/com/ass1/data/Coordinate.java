package com.ass1.data;

public class Coordinate {
	double latitude;
	double longitude;

	public Coordinate(double latitude, double longitude) {
		if (latitude < -90 || latitude > 90) {
			throw new IllegalArgumentException("Latitude range is -90 to 90. Got " + latitude);
		}
		if (longitude < -180 || longitude > 180) {
			throw new IllegalArgumentException("Longitude range is -180 to 180. Got " + longitude);
		}
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Coordinate(String duplet) {
		String[] pair = duplet.split(",");

		this.latitude = Double.parseDouble(pair[0]);
		this.longitude = Double.parseDouble(pair[1]);

		if (this.latitude < -90 || this.latitude > 90) {
			throw new IllegalArgumentException("Latitude range is -90 to 90. Got " + latitude);
		}
		if (this.longitude < -180 || this.longitude > 180) {
			throw new IllegalArgumentException("Longitude range is -180 to 180. Got " + longitude);
		}
	}

	public double getLatitude() {
		return this.latitude;
	}

	public double getLongitude() {
		return this.longitude;
	}

	public String toString() {
		String ns = (latitude >= 0 ? latitude + "°N" : -latitude + "°S");
		String ew = (longitude >= 0 ? longitude + "°E" : -longitude + "°W");
		return ns + ", " + ew;
	}
};
