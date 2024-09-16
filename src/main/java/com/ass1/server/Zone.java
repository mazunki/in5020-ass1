package com.ass1.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import com.ass1.*;

public class Zone implements Identifiable, Comparable<Zone> {
	HashMap<ServerInterface, Integer> servers = new HashMap<ServerInterface, Integer>();
	Random random = new Random();
	int maxRequests = 18;
	int ongoingRequests = 0;
	Identifier id;

	public Zone(Identifier id) {
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

		this.ongoingRequests++;
		for (Map.Entry<ServerInterface, Integer> pair : this.servers.entrySet()) {
			if (pair.getValue() == 0) {
				ServerInterface s = pair.getKey();
				this.servers.put(s, 1);
				return s;
			}
		}
		ServerInterface s = (new ArrayList<>(this.servers.keySet())).get(random.nextInt(this.servers.size()));
		this.servers.put(s, this.servers.get(s) + 1);
		return s;
	}

	public void releaseServer(ServerInterface s) {
		this.ongoingRequests--;
		this.servers.put(s, this.servers.get(s) - 1);
	}

	public boolean isChilling() {
		/* aka not overloaded */
		return this.ongoingRequests < this.maxRequests;
	}

	public String toString() {
		return "Zone<"
				+ this.id + ", "
				+ "" + String.valueOf(this.servers.size()) + " online, "
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
