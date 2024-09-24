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
import java.util.logging.Logger;

import com.ass1.*;
import com.ass1.server.*;

public class ProxyServer extends UnicastRemoteObject implements ProxyServerInterface {
	private static final Logger logger = LoggerUtil.createLogger(ProxyServer.class.getName(), "server", "proxy");

	WraparoundTreeSet<Zone> zones = new WraparoundTreeSet<Zone>(); // guarantees order of elements, a formal
									// requirement
	int maxNeighbourDistance = 2;
	Registry registry;

	private boolean universe_exists = true;

	public ProxyServer(int port) throws RemoteException {
		logger.info("Starting proxy server");

		this.registry = LocateRegistry.createRegistry(port);
		registry.rebind(PROXY_IDENTIFIER, this);
		logger.info("ProxyServer listening on port " + port);
	}

	public synchronized void register(Zone zone) throws RemoteException {
		if (this.zones.getObject(zone) == null) {
			zones.add(zone);
			logger.info("Added new zone: " + zone);
		} else {
			logger.info("Attmpted to register duplicate zone: " + zone.getId());
		}
	}

	public synchronized void registerZone(Identifier zoneId) throws RemoteException {
		if (this.zones.getObjectById(zoneId) == null) {
			Zone zone = new Zone(zoneId);
			this.zones.add(zone);
			logger.info("Added new zone: " + zone + ". Currently got " + this.zones.size() + " zones.");
		} else {
			logger.info("Attmpted to register duplicate zone: " + zoneId);
		}
	}

	public synchronized void startupTask(ServerInterface server, Identifier zoneId) throws RemoteException {
		Zone zone = this.zones.getObjectById(zoneId);
		String pre = zone.toString();
		zone.grabServer(server);
		logger.fine("Starting task from " + pre + " => " + zone);
	}

	public synchronized void completeTask(ServerInterface server, Identifier zoneId) throws RemoteException {
		Zone zone = this.zones.getObjectById(zoneId);
		String pre = zone.toString();
		zone.releaseServer(server);
		logger.fine("Released task from " + pre + " => " + zone);
	}

	public void unregister(ServerInterface server, Identifier zoneId, Identifier serverId) throws RemoteException {
		logger.fine("Server '" + serverId + "' wants to leave from " + zoneId);
		Zone zone = this.zones.getObjectById(zoneId);
		zone.forget(server);
		logger.info("Server '" + serverId + "' left " + zone);
	}

	public void register(ServerInterface server, Identifier zoneId, Identifier serverId) throws RemoteException {
		Zone zone = zones.getObjectById(zoneId);
		if (zone == null) {
			this.registerZone(zoneId);
			zone = zones.getObjectById(zoneId);
		}

		zone.register(server);
		logger.info("Registered new server '" + serverId + "' on " + zone);
	}

	public ServerInterface getServer(Identifier zoneId) throws NoSuchObjectException {
		Zone local_zone = this.zones.getObjectById(zoneId);

		if (local_zone.isAvailable()) {
			logger.fine("Found local zone for " + zoneId + ": " + local_zone);
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
			// no extern zones were helpful, let's just do it ourselves. sigh.
			logger.fine("Local zone was busy, but so was everyone else. Using " + local_zone);
			return local_zone.getServer();
		}

		extern_zones.sort(Comparator.comparingInt(Zone::getRequestCount));

		logger.fine("Local zone was busy. Found " + extern_zones.size()
				+ " volunteering neighbours. Using " + extern_zones.getFirst());

		return extern_zones.getFirst().getServer();
	}

	public boolean isAlive() {
		return this.universe_exists;
	}

	public void start() {
		this.universe_exists = true;
		logger.info("ProxyServer started and ready to receive servers");
		this.spin();
	}

	public void stop() {
		logger.info("Got request to terminate universe. Current zonecount: " + this.zones.size());
		for (Zone zone : this.zones) {
			try {
				zone.kill_everyone();
			} catch (RemoteException e) {
				logger.severe("Couldn't stop " + zone);
			}
		}
		this.universe_exists = false;
		logger.info("There is no universe. Get lost.");

		try {
			UnicastRemoteObject.unexportObject(this, true);
		} catch (NoSuchObjectException e) {
			logger.severe("Couldn't unexport proxy server");
		}

	}

	public void spin() {
		while (this.isAlive()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				this.stop();
			}
		}
		logger.info("Stopping proxy loop. Bye!");
	}

	public static void main(String[] args) throws RemoteException {
		ProxyServer proxyserver = new ProxyServer(1099);
		proxyserver.start();
	}
}
