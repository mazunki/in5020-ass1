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
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.rmi.NotBoundException;

import com.ass1.loadbalancer.*;
import com.ass1.*;

public class ServerStub implements ServerInterface {
	private static final Logger logger = Logger.getLogger(ServerStub.class.getName());
	private static final Level logLevel = Level.INFO;
	private static final OnelineFormatter fmt = new OnelineFormatter("stub");
	private static final ConsoleHandler consHandler = new ConsoleHandler();
	private static FileHandler fileHandler;

	Server server;
	Identifier zoneId;
	ProxyServerInterface proxyServer;

	private ExecutorService executor;
	int counter = 0;
	private static int REPORT_INTERVAL = 18;

	public ServerStub(Server server, Identifier zone) throws RemoteException {
		if (fileHandler == null) {
			try {
				fileHandler = new FileHandler("log/server.log", true);
				fileHandler.setFormatter(fmt);
				logger.addHandler(fileHandler);

				consHandler.setFormatter(fmt);
				logger.addHandler(consHandler);
				logger.setUseParentHandlers(false);

				logger.setLevel(ServerStub.logLevel);
			} catch (IOException e) {
				System.err.println("Failed to initialize logger: " + e.getMessage());
			}
		}

		this.zoneId = zone;
		this.server = server;
		this.executor = Executors.newFixedThreadPool(1);
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

	private <T> T execute(Callable<T> task) throws RemoteException {
		logger.finer("Received task on " + this.getRegistryName());
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
			return result;
		} catch (ExecutionException e) {
			throw new RemoteException("[serverstub] Task execution failed");
		} catch (InterruptedException e) {
			throw new RemoteException("[serverstub] Task execution was cancelled");
		}
	}

	public int getPopulationOfCountry(String[] args) throws RemoteException {
		return this.execute(() -> this.server.getPopulationOfCountry(args));
	}

	public int getPopulationOfCountry(String countryName) throws RemoteException {
		return this.execute(() -> this.server.getPopulationOfCountry(countryName));
	}

	public int getNumberOfCities(String[] args) throws RemoteException {
		return this.execute(() -> this.server.getNumberOfCities(args));
	}

	public int getNumberOfCities(String countryName, int minPopulation) throws RemoteException {
		return this.execute(() -> this.server.getNumberOfCities(countryName, minPopulation));
	}

	public int getNumberOfCountries(String[] args) throws RemoteException {
		return this.execute(() -> this.server.getNumberOfCountries(args));
	}

	public int getNumberOfCountries(int cityCount, int minPopulation) throws RemoteException {
		return this.execute(() -> this.server.getNumberOfCountries(cityCount, minPopulation));
	}

	public int getNumberOfCountries(int cityCount, int minPopulation, int maxPopulation) throws RemoteException {
		return this.execute(() -> this.server.getNumberOfCountries(cityCount, minPopulation, maxPopulation));
	}

	public boolean locatedAt(Identifier zoneId) {
		return this.zoneId.equals(zoneId);
	}

}
