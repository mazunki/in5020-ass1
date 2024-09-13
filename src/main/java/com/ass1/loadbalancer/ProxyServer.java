package com.ass1.loadbalancer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;

import com.ass1.*;
import com.ass1.server.*;

public class ProxyServer {
	WraparoundTreeSet<Zone> zones = new WraparoundTreeSet<Zone>(); // guarantees order of elements, a formal
									// requirement
	int maxNeighbourDistance = 2;

	public ProxyServer(int port) throws RemoteException {
		System.out.println("Starting proxy server...");

		Registry registry = LocateRegistry.createRegistry(port);
		System.out.println("ProxyServer is listening on port " + port);
	}

	public void start() {
		while (true) {
		}
	}

	public static void main(String[] args) throws RemoteException {
		ProxyServer proxyserver = new ProxyServer(1099);
		proxyserver.start();

	}

	public Server getServer(Identifier zoneId) {
		Iterator<Zone> neigbhours = this.zones.iterator(this.zones.getObjectById(zoneId),
				this.maxNeighbourDistance);

		while (neigbhours.hasNext()) {
			Zone zone = neigbhours.next();
			if (zone.isChilling()) {
				return zone.getServer();
			}
		}

		return null;
	}
}
