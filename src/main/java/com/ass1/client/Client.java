package com.ass1.client;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ass1.server.*;
import com.ass1.*;
import com.ass1.loadbalancer.ProxyServerInterface;

public class Client {
	final static String PROXY_SERVER = "127.0.0.1";
	final static int PROXY_PORT = 1099;
	final static int CacheStr = 45; // Client cache limit
	Identifier zoneId;

	private Registry proxyRegistry;
	private ProxyServerInterface proxyServer;
	private ServerInterface server;
	private QueryResultCache cache;

	private static final Logger logger = LoggerUtil.createLogger(ServerStub.class.getName(), "client", "client");

	public Client(String zoneId) {
		this(zoneId, true);
	}

	public Client(String zoneId, boolean enableCache) {
		this.zoneId = new Identifier(zoneId);

		if (enableCache) {
			Logger cacheLogger = LoggerUtil.deriveLogger(Client.logger, "cache", Level.INFO);
			this.cache = new QueryResultCache(QueryResultCache.DEFAULT_CLIENT_CACHE_LIMIT, cacheLogger);
		}

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
			Identifier serverId = this.proxyServer.findServer(new Identifier(zoneId));
			try {
				this.server = (ServerInterface) this.proxyRegistry.lookup(serverId.toString());
			} catch (NotBoundException e) {
				throw new RuntimeException(
						"Tried to look up an invalid handle for a server on the registry: "
								+ serverId);
			}
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
		if (this.cache != null && this.cache.has(method, args)) {
			return this.cache.get(method, args); // Return cached result
		}

		Object result = null;
		String method_name = method.toLowerCase();

		try {
			switch (method_name) {
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

		if (this.cache == null) {
			return result;
		}
		return this.cache.remember(method, args, result);
	}

	public static void main(String[] args) {
		/*
		 * clients are invoked with the following argument pattern
		 * <method> [options] [<arg1> <arg2> <arg3> ...] <zone>
		 */
		List<String> arguments = new ArrayList<String>(Arrays.asList(args));

		boolean use_cache = arguments.remove("--cache");

		if (arguments.size() < 2) {
			throw new IllegalArgumentException(
					"At minimum, specify which method to run, and which zone to connect to");
		}

		String method = arguments.get(0);
		String zoneId = arguments.get(arguments.size() - 1).replaceFirst("Zone:", "");
		List<String> query_args = arguments.subList(1, arguments.size() - 1);

		Client client = new Client(zoneId, use_cache);
		Object response = client.makeQuery(method, query_args.toArray(new String[0]));
		System.out.println(response);
	}
}
