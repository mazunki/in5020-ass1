package com.ass1.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.ass1.Identifier;

public interface ServerInterface extends Remote {
	// Object call(String method, Object[] args) throws RemoteException;

	void simulateExecutionDelay() throws RemoteException;

	boolean locatedAt(Identifier zoneId) throws RemoteException;

	int getPopulationOfCountry(String[] args) throws RemoteException;

	int getPopulationOfCountry(String countryName) throws RemoteException;

	int getNumberOfCities(String[] args) throws RemoteException;

	int getNumberOfCities(String countryName, int minPopulation) throws RemoteException;

	int getNumberOfCountries(String[] args) throws RemoteException;

	int getNumberOfCountries(int cityCount, int minPopulation) throws RemoteException;

	int getNumberOfCountries(int cityCount, int minPopulation, int maxPopulation) throws RemoteException;

}
