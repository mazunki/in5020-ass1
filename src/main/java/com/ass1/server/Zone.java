package com.ass1.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import com.ass1.*;

public class Zone implements Identifiable, Comparable<Zone> {
	HashMap<Server, Integer> servers = new HashMap<Server, Integer>();
	Random random = new Random();
	int maxRequests = 18;
	int ongoingRequests = 0;
	Identifier id;

	public Zone(int id) {
		this.id = new Identifier(id);
	}

	public void register(Server server) {
		this.servers.put(server, 0);
	}

	public void forget(Server server) {
		this.servers.remove(server);
	}

	public Server getServer() {
		/*
		 * returns the first idle server on the zone, falling back to a random one.
		 * assumes zone has at least one server in its pool
		 */
		if (this.servers.size() == 0) {
			return null;
		}

		this.ongoingRequests++;
		for (Map.Entry<Server, Integer> pair : this.servers.entrySet()) {
			if (pair.getValue() == 0) {
				Server s = pair.getKey();
				this.servers.put(s, 1);
				return s;
			}
		}
		Server s = (new ArrayList<>(this.servers.keySet())).get(random.nextInt(this.servers.size()));
		this.servers.put(s, this.servers.get(s) + 1);
		return s;
	}

	public void releaseServer(Server s) {
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
				+ "#" + String.valueOf(this.servers.size()) + ", "
				+ String.valueOf(this.ongoingRequests) + "/" + String.valueOf(this.maxRequests)
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
