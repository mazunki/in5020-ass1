package com.ass1.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class GeonameLoader {
	private static String datasetFilePath = "src/main/resources/exercise_1_dataset.csv";
	private Integer limit;

	public GeonameLoader(Integer limit) {
		this.limit = limit;
	}

	public static HashMap<Integer, Geoname> loadCityData(Integer limit) throws IllegalArgumentException {
		GeonameLoader loader = new GeonameLoader(limit);
		return loader.loadCityData();
	}

	public HashMap<Integer, Geoname> loadCityData() {
		HashMap<Integer, Geoname> entries = new HashMap<Integer, Geoname>();

		try (BufferedReader br = new BufferedReader(new FileReader(datasetFilePath))) {
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

	public static List<Geoname> getByName(String countryName) {
		ArrayList<Geoname> cities = new ArrayList<Geoname>();
		try (BufferedReader br = new BufferedReader(new FileReader(datasetFilePath))) {
			String line;
			br.readLine(); // Skip the header

			while ((line = br.readLine()) != null) {
				String[] fields = line.split(";");

				try {
					Geoname g = Geoname.fromCSVEntry(fields);

					if (g.countryName.equals(countryName)) {
						cities.add(g);
					}
				} catch (IllegalArgumentException e) {
					System.out.println("Skipping invalid entry: " + line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return cities;
	}

	public static List<String> getAllCountryNames() {
		List<String> countryNames = new ArrayList<>();
		HashMap<String, Boolean> countryNameMap = new HashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader(datasetFilePath))) {
			String line;
			br.readLine(); // Skip the header

			while ((line = br.readLine()) != null) {
				String[] fields = line.split(";");

				try {
					Geoname g = Geoname.fromCSVEntry(fields);

					// Add country names to the list if they haven't been encountered before
					if (!countryNameMap.containsKey(g.countryName)) {
						countryNames.add(g.countryName);
						countryNameMap.put(g.countryName, true);
					}
				} catch (IllegalArgumentException e) {
					System.out.println("Skipping invalid entry: " + line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return countryNames;
	}

	public static class GeonameIterator implements Iterator<Geoname> {
		private BufferedReader br;
		private String currentLine;

		public GeonameIterator() {
			try {
				br = new BufferedReader(new FileReader(datasetFilePath));
				br.readLine(); // Skip header
				currentLine = br.readLine(); // Initialize the first line
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean hasNext() {
			return currentLine != null;
		}

		@Override
		public Geoname next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			String[] fields = currentLine.split(";");
			try {
				Geoname city = Geoname.fromCSVEntry(fields);
				currentLine = br.readLine();
				return city;
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				currentLine = null;
				throw new NoSuchElementException("Invalid entry found, skipping.");
			}
		}

		public void close() {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static Iterable<Geoname> getCities() {
		return () -> new GeonameIterator();
	}

	public static GeonameIterator iterator() {
		return new GeonameIterator();
	}
}
