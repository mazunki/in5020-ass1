package com.ass1.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;
import java.rmi.NotBoundException;

import com.ass1.loadbalancer.*;
import com.ass1.*;

public class ServerStub implements ServerInterface {
	private final Logger logger;

	Server server;
	Identifier zoneId;
	ProxyServerInterface proxyServer;

	private ExecutorService executor;
	int counter = 0;
	private static int REPORT_INTERVAL = 18;
	QueryResultCache cache;

	public ServerStub(Server server, Identifier zone) throws RemoteException {
		this.zoneId = zone;
		this.server = server;
		this.logger = LoggerUtil.createLogger("server-" + this.getRegistryName(), "server", "stub");
		this.executor = Executors.newFixedThreadPool(1);
		this.cache = new QueryResultCache(QueryResultCache.DEFAULT_SERVER_CACHE_LIMIT,
				server.getId().toString());
		this.registerToProxyServer();
	}

	private void registerToProxyServer() throws RemoteException {
		String serverRegister = this.getRegistryName();

		logger.info("Connecting to ProxyServer from " + serverRegister);

		Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);

		ServerInterface srv = (ServerInterface) UnicastRemoteObject.exportObject(this, 0);

		try {
			proxyServer = (ProxyServerInterface) registry.lookup(ProxyServer.PROXY_IDENTIFIER);
		} catch (NotBoundException e) {
			throw new RuntimeException("Could not find anywhere to register ourselves");
		}

		proxyServer.register(srv, this.zoneId, this.server.getId());

		logger.info("Registered " + serverRegister + " on proxy server");
	}

	public void addNetworkDelay() {
		try {
			Thread.sleep(80);
		} catch (InterruptedException e) {
			throw new RuntimeException("Couldn't add network delay");
		}
	}

	public String getRegistryName() {
		return "server-" + this.server + "@zone-" + this.zoneId;
	}

	public String getWorkload() {
		return this.executor.toString();
	}

	public void enter() {
		if (++this.counter % ServerStub.REPORT_INTERVAL == 0) {
			this.reportStatus();
		}
	}

	public void leave() {
	}

	public boolean isAlive() {
		return true;
	}

	public void spin() {
		ServerInterface self = this;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					logger.info("Shutting down " + server);
					proxyServer.unregister(self, zoneId, server.getId());
				} catch (RemoteException e) {
					logger.warning("Failed to unregister " + getRegistryName());
				}
			}
		});

		while (true) {
		}
	}

	public void simulateExecutionDelay() {
		try {
			Thread.sleep(ServerInterface.EXECUTION_DELAY);
		} catch (InterruptedException e) {
			throw new RuntimeException("Couldn't simulate execution delay");
		}
	}

	public void reportStatus() {
		ThreadPoolExecutor ex = (ThreadPoolExecutor) this.executor;
		logger.info("Execution queue contains " + ex.getQueue().size()
				+ " tasks for " + this.getRegistryName());
	}

	private <T> T execute(Callable<T> task, Object[] cache_args, Class<T> return_type) throws RemoteException {
		logger.finer("Received task on " + this.getRegistryName());

		/*
		 * sorta cursed, but should work. avoids redundancy, and
		 * accidental typos. sorta wish i could fetch the arguments
		 * like this to, to avoid rebuilding `Object[] cache_args`
		 * during wrapping
		 */
		String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		if (cache.has(methodName, cache_args)) {
			return return_type.cast(cache.get(methodName, cache_args));
		}

		try {
			Future<T> future = this.executor.submit(() -> {
				this.simulateExecutionDelay();
				return task.call();
			});
			logger.finer("Submitted task on " + this.getRegistryName());
			this.proxyServer.startupTask((ServerInterface) this, this.zoneId);
			T result = future.get();
			this.proxyServer.completeTask((ServerInterface) this, this.zoneId);
			logger.fine("Completed task on " + this.getRegistryName());
			cache.remember(methodName, cache_args, result);
			return result;

		} catch (ExecutionException e) {
			throw new RemoteException("Task execution failed");
		} catch (InterruptedException e) {
			throw new RemoteException("Task execution was cancelled");
		}
	}

	public Integer getPopulationOfCountry(String[] args) throws RemoteException {
		Object[] cacheArgs = (Object[]) args;
		return this.execute(() -> this.server.getPopulationOfCountry(args), cacheArgs, Integer.class);
	}

	public Integer getPopulationOfCountry(String countryName) throws RemoteException {
		Object[] cacheArgs = { countryName };
		return this.execute(() -> this.server.getPopulationOfCountry(countryName), cacheArgs, Integer.class);
	}

	public Integer getNumberOfCities(String[] args) throws RemoteException {
		Object[] cacheArgs = (Object[]) args;
		return this.execute(() -> this.server.getNumberOfCities(args), cacheArgs, Integer.class);
	}

	public Integer getNumberOfCities(String countryName, int minPopulation) throws RemoteException {
		Object[] cacheArgs = { countryName, minPopulation };
		return this.execute(() -> this.server.getNumberOfCities(countryName, minPopulation), cacheArgs,
				Integer.class);
	}

	public Integer getNumberOfCountries(String[] args) throws RemoteException {
		Object[] cacheArgs = (Object[]) args;
		return this.execute(() -> this.server.getNumberOfCountries(args), cacheArgs, Integer.class);
	}

	public Integer getNumberOfCountries(int cityCount, int minPopulation) throws RemoteException {
		Object[] cacheArgs = { cityCount, minPopulation };
		return this.execute(() -> this.server.getNumberOfCountries(cityCount, minPopulation), cacheArgs,
				Integer.class);
	}

	public Integer getNumberOfCountries(int cityCount, int minPopulation, int maxPopulation)
			throws RemoteException {
		Object[] cacheArgs = { cityCount, minPopulation, maxPopulation };
		return this.execute(() -> this.server.getNumberOfCountries(cityCount,
				minPopulation, maxPopulation), cacheArgs, Integer.class);

	}

	public boolean locatedAt(Identifier zoneId) {
		return this.zoneId.equals(zoneId);
	}

}
