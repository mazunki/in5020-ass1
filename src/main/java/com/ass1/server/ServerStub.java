package com.ass1.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.rmi.NotBoundException;

import com.ass1.loadbalancer.*;
import com.ass1.*;

public class ServerStub implements ServerInterface {

	Server server;
	Identifier id, zoneId;
	ProxyServerInterface proxyServer;

	volatile boolean world_permits_my_existence;

	private ExecutorService executor;
	int counter = 0;
	private static int REPORT_INTERVAL = 18;
	private HashMap<String, List<Long>> meter_execution = new HashMap<String, List<Long>>();
	private HashMap<String, List<Long>> meter_waiting = new HashMap<String, List<Long>>();

	QueryResultCache cache;
	private static final Logger logger = LoggerUtil.createLogger(Server.class.getName(), "server", "server");

	public ServerStub(String serverId, String zoneId) throws RemoteException {
		this(new Identifier(serverId), new Identifier(zoneId));
	}

	public ServerStub(Identifier serverId, Identifier zoneId) throws RemoteException {
		this(new Server(), serverId, zoneId, true);
	}

	public ServerStub(Server server, Identifier serverId, Identifier zone, boolean enableCache)
			throws RemoteException {
		this.zoneId = zone;
		this.server = server;
		this.id = serverId;

		if (enableCache) {
			Logger cacheLogger = LoggerUtil.deriveLogger(ServerStub.logger, "cache", Level.INFO);
			this.cache = new QueryResultCache(QueryResultCache.DEFAULT_SERVER_CACHE_LIMIT, cacheLogger);
		}
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

		proxyServer.register(srv, this.zoneId, this.id);

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
		return "server-" + this.id + "@zone-" + this.zoneId;
	}

	public String getWorkload() {
		return this.executor.toString();
	}

	public void enter() {
		if (++this.counter % ServerStub.REPORT_INTERVAL == 0) {
			this.reportStatus();
		}
	}

	private void spin() {
		while (this.isAlive()) {
		}
	}

	public void launch() throws RemoteException {
		this.executor = Executors.newFixedThreadPool(1);
		this.cache = new QueryResultCache(QueryResultCache.DEFAULT_SERVER_CACHE_LIMIT,

				this.id.toString());
		this.registerToProxyServer();
		this.world_permits_my_existence = true;

		try {
			this.spin();
		} finally {
			this.executor.shutdown();
			try {
				while (!this.executor.awaitTermination(50, TimeUnit.MILLISECONDS)) {
					this.executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				this.executor.shutdownNow();
				logger.warning("Interrupted execution thread");
			}
		}
	}

	public void leave() {
	}

	public boolean isAlive() {
		return this.world_permits_my_existence;
	}

	public void report_measurements() {
		for (Entry<String, List<Long>> entry : this.meter_execution.entrySet()) {
			double avg = entry.getValue().stream().mapToDouble(Long::longValue).average().orElse(0.0);
			double min = entry.getValue().stream().mapToDouble(Long::longValue).min().orElse(0.0);
			double max = entry.getValue().stream().mapToDouble(Long::longValue).max().orElse(0.0);
			logger.info(entry.getKey() +
					" execution time: avg " + avg + "ms, min " + min + "ms, max " + max + "ms");
		}
		for (Entry<String, List<Long>> entry : this.meter_waiting.entrySet()) {
			double avg = entry.getValue().stream().mapToDouble(Long::longValue).average().orElse(0.0);
			double min = entry.getValue().stream().mapToDouble(Long::longValue).min().orElse(0.0);
			double max = entry.getValue().stream().mapToDouble(Long::longValue).max().orElse(0.0);
			logger.info(entry.getKey() +
					" waiting time: avg " + avg + "ms, min " + min + "ms, max " + max + "ms");
		}
	}

	public void terminate() throws RemoteException {
		this.world_permits_my_existence = false;

		logger.info("Shutting down " + this.id);
		this.report_measurements();

		proxyServer.unregister((ServerInterface) this, zoneId, this.id);
		logger.info(this.id + " is now offline.");

		UnicastRemoteObject.unexportObject(this, true);
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

	public void addExecutionMeasurement(String method, long start, long end) {
		long duration = end - start;
		List<Long> avgs = this.meter_execution.getOrDefault(method, new ArrayList<>());
		avgs.add(duration);
		this.meter_execution.put(method, avgs);
	}

	public void addWaitingTime(String method, long start, long end) {
		long duration = end - start;
		List<Long> avgs = this.meter_waiting.getOrDefault(method, new ArrayList<>());
		avgs.add(duration);
		this.meter_waiting.put(method, avgs);
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
				long start_exec_time = System.currentTimeMillis();
				this.simulateExecutionDelay();
				T result = task.call();
				this.addExecutionMeasurement(methodName, start_exec_time,
						System.currentTimeMillis());

				return result;
			});
			logger.finer("Submitted task on " + this.getRegistryName());
			this.proxyServer.startupTask((ServerInterface) this, this.zoneId);

			long start_time = System.currentTimeMillis();
			T result = future.get();
			this.addWaitingTime(methodName, start_time, System.currentTimeMillis());

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

	public static void main(String[] args) throws RemoteException {
		if (args.length < 2) {
			throw new IllegalArgumentException("Please provide a zone name and a server identifier");
		}

		// TODO: consider adding support for automatic server identifier names for zone

		ServerStub stub = new ServerStub(args[1], args[0]);
		stub.launch();
	}
}
