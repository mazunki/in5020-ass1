package com.ass1.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    // Methods list
    int getPopulationofCountry(String countryName) throws RemoteException;
    int getNumberofCities(String countryName, int minPopulation) throws RemoteException;
    int getNumberofCountries(int cityCount, int minPopulation) throws RemoteException;
    int getNumberofCountries(int cityCount, int minPopulation, int maxPopulation) throws RemoteException;

    
}



