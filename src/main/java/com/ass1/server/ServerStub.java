package com.ass1.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;

import com.ass1.loadbalancer.*;
import com.ass1.*;

public class ServerStub extends RMISocketFactory implements ServerInterface {
	Server server;
	Identifier zoneId;
	ProxyServerInterface proxyServer;

	private ExecutorService executor;

	public ServerStub(Server server, Identifier zone) throws RemoteException {
		this.zoneId = zone;
		this.server = server;
		this.executor = Executors.newFixedThreadPool(1);
		this.registerToProxyServer();
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		System.out.println("[serverstub] Got connection");
		try {
			Thread.sleep(80);
		} catch (InterruptedException e) {
			throw new RuntimeException("[serverstub] Couldn't sleep during socket creation");
		}
		return new Socket(host, port);
	}

	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		return new ServerSocket(port);
	}

	private void registerToProxyServer() throws RemoteException {
		String serverRegister = this.getRegistryName();

		System.out.println("[serverstub] Connecting to ProxyServer from " + serverRegister);

		Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);

		ServerInterface srv = (ServerInterface) UnicastRemoteObject.exportObject(this, 0);

		try {
			proxyServer = (ProxyServerInterface) registry.lookup(ProxyServer.PROXY_IDENTIFIER);
		} catch (NotBoundException e) {
			throw new RuntimeException("Could not find anywhere to register ourselves");
		}

		proxyServer.register(srv, this.zoneId, this.server.getId());

		System.out.println("[serverstub] Registered " + serverRegister + " on proxy server");
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

	public boolean isAlive() {
		return true;
	}

	public void spin() {
		ServerInterface self = this;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					System.out.println("[serverstub] Shutting down " + server);
					proxyServer.unregister(self, zoneId, server.getId());
				} catch (RemoteException e) {
					System.err.println("[serverstub] Failed to unregister " + getRegistryName());
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
		System.out.println("[serverstub] Execution queue contains " + ex.getPoolSize()
				+ " tasks for " + this.getRegistryName());
	}

	private <T> T execute(Callable<T> task) throws RemoteException {
		System.out.println("[serverstub] Received task on " + this.getRegistryName());
		this.reportStatus();
		try {
			Future<T> future = this.executor.submit(() -> {
				this.simulateExecutionDelay();
				return task.call();
			});
			System.out.println("[serverstub] Submitted task on " + this.getRegistryName());
			T result = future.get();
			this.proxyServer.completeTask((ServerInterface) this, this.zoneId);
			System.out.println("[serverstub] Completed task on " + this.getRegistryName());
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
