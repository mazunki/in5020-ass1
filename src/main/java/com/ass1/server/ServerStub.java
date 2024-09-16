package com.ass1.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;

import com.ass1.loadbalancer.*;
import com.ass1.*;

public class ServerStub extends RMISocketFactory implements ServerStubInterface {
	Server server;
	Identifier zoneId;
	ProxyServerInterface proxyServer;

	public ServerStub(Server server, Identifier zone) throws RemoteException {
		this.zoneId = zone;
		this.server = server;
		this.registerToProxyServer();
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		System.out.println("got connection");
		try {
			Thread.sleep(80);
		} catch (InterruptedException e) {
			throw new RuntimeException("couldn't sleep during socket creation");
		}
		return new Socket(host, port);
	}

	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		return new ServerSocket(port);
	}

	private void registerToProxyServer() throws RemoteException {
		System.out.println("Connecting to ProxyServer");

		Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);

		ServerInterface srv = (ServerInterface) UnicastRemoteObject.exportObject(this.server, 0);

		try {
			proxyServer = (ProxyServerInterface) registry.lookup(ProxyServer.PROXY_IDENTIFIER);
		} catch (NotBoundException e) {
			throw new RuntimeException("Could not find anywhere to register ourselves");
		}

		proxyServer.register(srv, this.zoneId, this.server.getId());

		String serverRegister = this.getRegistryName();
		System.out.println("Registered " + serverRegister + " on proxy server");
	}

	public Object call(String method, Object[] args) {
		Method callable;

		try {
			callable = server.getClass().getMethod(method, String[].class);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("No such method: '" + method + "'");
		}

		Object result;
		try {
			this.server.simulateExecutionDelay();
			result = callable.invoke(this.server, (Object) args);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("You are not allowed to run this method! 😡");
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Failed to invoke method...");
		}

		this.addNetworkDelay(); // response Server => client
		return result;

	}

	public void addNetworkDelay() {
		try {
			Thread.sleep(80);
		} catch (InterruptedException e) {
			throw new RuntimeException("couldn't sleep");
		}
	}

	public String getRegistryName() {
		return "server-" + this.server + "@zone-" + this.zoneId;
	}

	public boolean isAlive() {
		return true;
	}

	public void spin() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					proxyServer.unregister(server, zoneId, server.getId());
				} catch (RemoteException e) {
					System.err.println("failed to unregister " + getRegistryName());
				}
			}
		});

		while (true) {
		}
	}
}
