package com.ass1.loadbalancer;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.ass1.*;
import com.ass1.server.*;

public class ProxyServer extends UnicastRemoteObject implements ProxyServerInterface {
	WraparoundTreeSet<Zone> zones = new WraparoundTreeSet<Zone>(); // guarantees order of elements, a formal
									// requirement
	int maxNeighbourDistance = 2;
	Registry registry;

	public ProxyServer(int port) throws RemoteException {
		System.out.println("[proxy] Starting proxy server");

		this.registry = LocateRegistry.createRegistry(port);
		registry.rebind(PROXY_IDENTIFIER, this);
		System.out.println("[proxy] ProxyServer listening on port " + port);
	}

	public void register(Zone zone) throws RemoteException {
		if (this.zones.getObject(zone) == null) {
			zones.add(zone);
			System.out.println("[proxy] Added new zone: " + zone);
		} else {
			System.err.println("[proxy] Attmpted to register duplicate zone: " + zone.getId());
		}
	}

	public void registerZone(Identifier zoneId) throws RemoteException {
		if (this.zones.getObjectById(zoneId) == null) {
			Zone zone = new Zone(zoneId);
			this.zones.add(zone);
			System.out.println("[proxy] Added new zone: " + zone);
		} else {
			System.err.println("[proxy] Attmpted to register duplicate zone: " + zoneId);
		}
	}

	public void completeTask(ServerInterface server, Identifier zoneId) throws RemoteException {
		this.zones.getObjectById(zoneId).releaseServer(server);
	}

	public void unregister(ServerInterface server, Identifier zoneId, Identifier serverId) throws RemoteException {
		System.out.println("[proxy] Server '" + serverId + "' wants to leave from " + zoneId);
		Zone zone = this.zones.getObjectById(zoneId);
		zone.forget(server);
		System.out.println("[proxy] Server '" + serverId + "' left " + zone);
	}

	public void register(ServerInterface server, Identifier zoneId, Identifier serverId) throws RemoteException {
		Zone zone = zones.getObjectById(zoneId);
		if (zone == null) {
			this.registerZone(zoneId);
			zone = zones.getObjectById(zoneId);
		}

		zone.register(server);
		System.out.println("[proxy] Registered new server '" + serverId + "' on " + zone);
	}

	public void releaseServer(ServerInterface server) throws RemoteException {
		Iterator<Zone> neigbhours = this.zones.iterator();
		while (neigbhours.hasNext()) {
			Zone z = neigbhours.next();
			if (server.locatedAt(z.getId())) {
				z.releaseServer(server);
				return;
			}
		}
	}

	public ServerInterface getServer(Identifier zoneId) throws NoSuchObjectException {
		Zone local_zone = this.zones.getObjectById(zoneId);

		if (local_zone.isAvailable()) {
			System.out.println("[proxy] Found local zone for " + zoneId + ": " + local_zone);
			return local_zone.getServer();
		}

		Iterator<Zone> zones = this.zones.iterator(local_zone, this.maxNeighbourDistance);

		List<Zone> extern_zones = new ArrayList<Zone>(2);
		while (zones.hasNext()) {
			Zone neighbour = zones.next();
			if (neighbour.isChilling()) {
				extern_zones.add(neighbour);
			}
		}

		if (extern_zones.size() == 0) {
			return local_zone.getServer();
		}

		extern_zones.sort(Comparator.comparingInt(Zone::getRequestCount));
		return extern_zones.getFirst().getServer();
	}

	public void start() {
		System.out.println("[proxy] ProxyServer started and ready to receive servers");
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
