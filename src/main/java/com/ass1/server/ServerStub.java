package com.ass1.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;

import com.ass1.loadbalancer.*;
import com.ass1.*;

public class ServerStub {
	Server server;
	Identifier zoneId;
	ProxyServerInterface proxyServer;

	public ServerStub(Server server, Identifier zone) throws RemoteException {
		this.zoneId = zone;
		this.server = server;
		this.registerToProxyServer();
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

	public String getRegistryName() {
		return "server-" + this.server + "@zone-" + this.zoneId;
	}

	public boolean isAlive() {
		return true;
	}

	public void spin() {
		while (true) {
		}
	}
}
