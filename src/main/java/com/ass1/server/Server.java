package com.ass1.server;

import java.util.LinkedHashMap;
import java.util.Map;

import java.rmi.RemoteException;
import java.util.List;

import com.ass1.data.Geoname;
import com.ass1.data.GeonameLoader;
import com.ass1.*;

public class Server implements ServerInterface {



	Identifier id;
	ServerStub stub;

	public Server(Identifier serverId, Identifier zoneId) throws RemoteException {
		this.id = serverId;
		this.stub = new ServerStub(this, zoneId);
	}

	public Server(String serverId, String zoneId) throws RemoteException {
		this.id = new Identifier(serverId);
		this.stub = new ServerStub(this, new Identifier(zoneId));
	}

	public static void main(String[] args) throws RemoteException {
		if (args.length < 2) {
			throw new IllegalArgumentException("Please provide a zone name and a server identifier");
		}

		// TODO: consider adding support for automatic server identifier names for zone

		Server server = new Server(args[1], args[0]);
		server.launch();
	}

	public boolean locatedAt(Identifier zoneId) {
		return this.stub.zoneId.equals(zoneId);
	}

	public void launch() {
		while (this.stub.isAlive()) {
			this.stub.spin();
		}
	}

	public int getPopulationOfCountry(String[] args) throws RemoteException {
		return this.getPopulationOfCountry(args[0]);
	}

	public int getPopulationOfCountry(String countryName) throws RemoteException {

		int population = 0;

		// Use the GeonameLoader to fetch cities for the given country name if not in cache
		List<Geoname> cities = GeonameLoader.getByName(countryName);

		for (Geoname city : cities) {
			population += city.getPopulation();
		}

		return population;
	}

	public int getNumberOfCities(String[] args) throws RemoteException {
		return this.getNumberOfCities(args[0], Integer.parseInt(args[1]));
	}

	public int getNumberOfCities(String countryName) throws RemoteException {
		// NOTE: this is not from spec, but simulation requires it
		return GeonameLoader.getByName(countryName).size();
	}

	public int getNumberOfCities(String countryName, int minPopulation) throws RemoteException {
		int cityCount = 0;

		List<Geoname> cities = GeonameLoader.getByName(countryName);

		for (Geoname city : cities) {
			if (city.getPopulation() >= minPopulation) {
				cityCount++;
			}
		}

		return cityCount;
	}

	public int getNumberOfCountries(String[] args) throws RemoteException {
		if (args.length == 3) {
			return this.getNumberOfCountries(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
					Integer.parseInt(args[2]));
		}
		return this.getNumberOfCountries(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
	}

	public int getNumberOfCountries(int cityCount, int minPopulation) throws RemoteException {
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

	public int getNumberOfCountries(int cityCount, int minPopulation, int maxPopulation) throws RemoteException {
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

	public Identifier getId() {
		return this.id;
	}

	public String toString() {
		return this.id.toString();
	}
}
