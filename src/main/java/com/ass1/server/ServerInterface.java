package com.ass1.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.ass1.Identifier;

public interface ServerInterface extends Remote {
	int EXECUTION_DELAY = 40; // ms

	boolean locatedAt(Identifier zoneId) throws RemoteException;

	void enter() throws RemoteException;

	void leave() throws RemoteException;

	void launch() throws RemoteException;

	void terminate() throws RemoteException;

	Integer getPopulationOfCountry(String[] args) throws RemoteException;

	Integer getPopulationOfCountry(String countryName) throws RemoteException;

	Integer getNumberOfCities(String[] args) throws RemoteException;

	Integer getNumberOfCities(String countryName, int minPopulation) throws RemoteException;

	Integer getNumberOfCountries(String[] args) throws RemoteException;

	Integer getNumberOfCountries(int cityCount, int minPopulation) throws RemoteException;

	Integer getNumberOfCountries(int cityCount, int minPopulation, int maxPopulation) throws RemoteException;
}
