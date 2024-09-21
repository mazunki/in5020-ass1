package com.ass1.client;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import com.ass1.server.*;
import com.ass1.*;
import com.ass1.loadbalancer.ProxyServerInterface;

public class Client {
	final static String PROXY_SERVER = "127.0.0.1";
	final static int PROXY_PORT = 1099;
	final static int CacheStr = 45; // Client cache limit
	Identifier zoneId;

	Registry proxyRegistry;
	ProxyServerInterface proxyServer;
	ServerInterface server;
	QueryResultCache cache;

	public Client(String zoneId) {
		this.zoneId = new Identifier(zoneId);

		// Check if cache should be enabled from command-line args
		boolean cacheEnabled = true; // TODO: Implement logic to check command-line args

		// Initialize cache based on whether caching is enabled
		this.cache = cacheEnabled ? new QueryResultCache(QueryResultCache.DEFAULT_CLIENT_CACHE_LIMIT)
				: new QueryResultCache(0);

		try {
			this.proxyRegistry = LocateRegistry.getRegistry(PROXY_SERVER, PROXY_PORT);
		} catch (RemoteException e) {
			throw new RuntimeException("Could not connect to proxy server... 😅");
		}

		try {
			this.proxyServer = (ProxyServerInterface) this.proxyRegistry
					.lookup(ProxyServerInterface.PROXY_IDENTIFIER);
		} catch (RemoteException e) {
			throw new RuntimeException("Failed to connect with proxy server! 😷" + e.getMessage());
		} catch (NotBoundException e) {
			throw new RuntimeException("Failed to find proxy server reference...");
		}

		try {
			this.server = this.proxyServer.getServer(new Identifier(zoneId));
		} catch (NoSuchObjectException e) {
			throw new RuntimeException(
					"Failed to get an available server for '" + this.zoneId
							+ "' neighbourhood... does this zone exist?");
		} catch (RemoteException e) {
			throw new RuntimeException("Failed to request a zone on proxy server! 😷");
		}
	}

	private void prepareServer() {
		try {
			this.server.enter();
			int ms = (this.server.locatedAt(this.zoneId)) ? Zone.LOCAL_DELAY : Zone.EXTERN_DELAY;
			Thread.sleep(ms);
		} catch (RemoteException e) {
			throw new RuntimeException("Failed to ask where server is located at");
		} catch (InterruptedException e) {
			throw new RuntimeException("Couldn't sleep on the (network) bus");
		}
	}

	public Object makeQuery(String method, String[] args) {
		if (this.cache.has(method, args)) {
			return this.cache.get(method, args); // Return cached result
		}

		Object result = null;

		try {
			switch (method.toLowerCase()) {
				case "getpopulationofcountry":
					switch (args.length) {
						case 1:
							this.prepareServer();
							result = this.server.getPopulationOfCountry(args[0]);
							break;
						default:
							throw new IllegalArgumentException(
									"This function requires 1 argument.");
					}
					break;
				case "getnumberofcities":
					switch (args.length) {
						case 2:
							this.prepareServer();
							result = this.server.getNumberOfCities(args[0],
									Integer.parseInt(args[1]));
							break;
						default:
							throw new IllegalArgumentException(
									"This function requires 2 arguments.");
					}
					break;
				case "getnumberofcountries":

					switch (args.length) {
						case 2:
							this.prepareServer();
							result = this.server.getNumberOfCountries(
									Integer.parseInt(args[0]),
									Integer.parseInt(args[1]));
							break;
						case 3:
							this.prepareServer();
							result = this.server.getNumberOfCountries(
									Integer.parseInt(args[0]),
									Integer.parseInt(args[1]),
									Integer.parseInt(args[2]));
							break;
						default:
							throw new IllegalArgumentException(
									"This function requires 2 or 3 arguments.");
					}
					break;
			}
		} catch (ClassCastException e) {
			throw new RuntimeException("Invalid typecasting performed");
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("Invalid number of arguments for function");
		} catch (RemoteException e) {
			throw new RuntimeException("Failed to call server");
		}

		// Store the result in cache after fetching it
		return this.cache.remember(method, args, result);
	}

	public static void main(String[] args) {
		/*
		 * clients are invoked with the following argument pattern
		 * <method> [<arg1> <arg2> <arg3> ...] <zone>
		 */
		if (args.length < 2) {
			throw new IllegalArgumentException(
					"At minimum, specify which method to run, and which zone to connect to");
		}
		String zoneId = args[args.length - 1].replaceFirst("Zone:", "");
		String method = args[0];
		String[] query_args = Arrays.copyOfRange(args, 1, args.length - 1);

		Client client = new Client(zoneId);
		Object response = client.makeQuery(method, query_args);
		System.out.println(response);
	}
}
