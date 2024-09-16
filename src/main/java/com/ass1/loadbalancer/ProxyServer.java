package com.ass1.loadbalancer;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;

import com.ass1.*;
import com.ass1.server.*;

public class ProxyServer extends UnicastRemoteObject implements ProxyServerInterface {
	WraparoundTreeSet<Zone> zones = new WraparoundTreeSet<Zone>(); // guarantees order of elements, a formal
									// requirement
	int maxNeighbourDistance = 2;
	Registry registry;

	public ProxyServer(int port) throws RemoteException {
		System.out.println("Starting proxy server");

		this.registry = LocateRegistry.createRegistry(port);
		registry.rebind(PROXY_IDENTIFIER, this);

		System.out.println("ProxyServer listening on port " + port);
	}

	public void register(Zone zone) throws RemoteException {
		if (this.zones.getObject(zone) == null) {
			zones.add(zone);
			System.out.println("Added new zone: " + zone);
		} else {
			System.err.println("Attmpted to register duplicate zone: " + zone.getId());
		}
	}

	public void registerZone(Identifier zoneId) throws RemoteException {
		if (this.zones.getObjectById(zoneId) == null) {
			Zone zone = new Zone(zoneId);
			this.zones.add(zone);
			System.out.println("Added new zone: " + zone);
		} else {
			System.err.println("Attmpted to register duplicate zone: " + zoneId);
		}
	}

	public void unregister(ServerInterface server, Identifier zoneId, Identifier serverId) throws RemoteException {
		Zone zone = this.zones.getObjectById(zoneId);
		zone.forget(server);
		System.out.println("Server '" + serverId + "' left " + zone);
	}

	public void register(ServerInterface server, Identifier zoneId, Identifier serverId) throws RemoteException {
		Zone zone = zones.getObjectById(zoneId);
		if (zone == null) {
			this.registerZone(zoneId);
			zone = zones.getObjectById(zoneId);
		}

		zone.register(server);
		System.out.println("Registered new server '" + serverId + "' on " + zone);
	}

	public ServerInterface getServer(Identifier zoneId) throws NoSuchObjectException {
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

	public void start() {
		System.out.println("ProxyServer started and ready to handle requests");
		while (true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				System.out.println("Shutting down Proxy Server");
				return;
			}
		}
	}

	public static void main(String[] args) throws RemoteException {
		ProxyServer proxyserver = new ProxyServer(1099);
		proxyserver.start();
	}
}
