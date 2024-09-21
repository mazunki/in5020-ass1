package com.ass1.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import com.ass1.*;

public class Zone implements Identifiable, Comparable<Zone> {
	private static final Logger logger = Logger.getLogger(Zone.class.getName());
	private static final Level logLevel = Level.INFO;
	private static final OnelineFormatter fmt = new OnelineFormatter("zone");
	private static final ConsoleHandler consHandler = new ConsoleHandler();
	private static FileHandler fileHandler;

	public final static int LOCAL_DELAY = 80; // ms
	public final static int EXTERN_DELAY = 170; // 80 + 90 = 170
	private static final int REPORT_INTERVAL = 18;

	HashMap<ServerInterface, Integer> servers = new HashMap<ServerInterface, Integer>();
	Random random = new Random();

	int maxRequests = 18;
	int limitRemoteThreshold = 8;

	AtomicInteger ongoingRequests = new AtomicInteger(0);
	AtomicInteger counter = new AtomicInteger(0);
	Identifier id;

	public Zone(Identifier id) {
		if (fileHandler == null) {
			try {
				fileHandler = new FileHandler("log/server.log", true);
				fileHandler.setFormatter(fmt);
				logger.addHandler(fileHandler);

				consHandler.setFormatter(fmt);
				logger.addHandler(consHandler);

				logger.setUseParentHandlers(false);
				logger.setLevel(Zone.logLevel);
			} catch (IOException e) {
				System.err.println("Failed to initialize logger: " + e.getMessage());
			}
		}
		this.id = id;
	}

	public Zone(int id) {
		this(new Identifier(id));
	}

	public void register(ServerInterface server) {
		this.servers.put(server, 0);
	}

	public void forget(ServerInterface server) {
		this.servers.remove(server);
	}

	public Server getObjectById(Identifier serverId) {
		throw new RuntimeException("wtf was this?");
	}

	public ServerInterface getServer() {
		/*
		 * returns the first idle server on the zone, falling back to a random one.
		 * assumes zone has at least one server in its pool
		 */
		if (this.servers.size() == 0) {
			return null;
		}

		for (Map.Entry<ServerInterface, Integer> pair : this.servers.entrySet()) {
			if (pair.getValue() == 0) {
				ServerInterface s = pair.getKey();
				logger.fine("Found idling server in " + this);
				this.servers.put(s, 1);
				return s;
			}
		}
		logger.fine("All servers in zone " + this + " were busy!");
		ServerInterface s = (new ArrayList<>(this.servers.keySet())).get(random.nextInt(this.servers.size()));
		return s;
	}

	public void grabServer(ServerInterface s) {
		this.ongoingRequests.incrementAndGet();
		if (this.counter.incrementAndGet() % Zone.REPORT_INTERVAL == 0) {
			this.reportStatus();
		}
		try {
			s.enter();
		} catch (RemoteException e) {
			logger.warning("Couldn't grab server");
		}
		synchronized (this.servers) {
			this.servers.put(s, this.servers.get(s) + 1);
		}
		logger.finer(" Grabbed a request slot on " + this);
	}

	public void releaseServer(ServerInterface s) {
		try {
			s.leave();
		} catch (RemoteException e) {
			logger.warning("Couldn't release server");
		}
		this.ongoingRequests.decrementAndGet();

		synchronized (this.servers) {
			this.servers.put(s, this.servers.get(s) - 1);
		}
		logger.finer("Released a request slot on " + this);
	}

	public int getRequestCount() {
		return this.ongoingRequests.get();
	}

	public boolean isChilling() {
		/* is keen on accepting remote zone's work */
		return this.ongoingRequests.get() < this.limitRemoteThreshold;
	}

	public boolean isAvailable() {
		/* aka not overloaded */
		return this.ongoingRequests.get() < this.maxRequests;
	}

	public void reportStatus() {
		logger.info(this.toString());
	}

	public String toString() {
		return "Zone<"
				+ this.id + ", "
				+ "" + String.valueOf(this.servers.size()) + " servers online, "
				+ String.valueOf(this.ongoingRequests) + "/" + String.valueOf(this.maxRequests)
				+ " requests"
				+ ">";
	}

	public Identifier getId() {
		return this.id;
	}

	public void setId(Identifier id) {
		this.id = id;
	}

	public int compareTo(Zone z) {
		return this.id.getValue().compareTo(z.getId().getValue());
	}
}
