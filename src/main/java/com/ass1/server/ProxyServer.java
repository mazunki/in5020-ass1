package com.ass1.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.rmi.AlreadyBoundException;
import com.ass1.*;

public class ProxyServer {
	WraparoundTreeSet<Zone> zones = new WraparoundTreeSet<Zone>(); // guarantees order of elements, a formal
									// requirement
	int maxNeighbourDistance = 2;

	public static void main(String[] args) {
		try {
			Registry registry = LocateRegistry.createRegistry(1234);
			Server server = new Server();
			ServerInterface serverStub = (ServerInterface) UnicastRemoteObject.exportObject(server, 0);
			registry.bind("server", serverStub);

			while (true) {
			}
		} catch (RemoteException | AlreadyBoundException e) {
			e.printStackTrace();
		}

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
