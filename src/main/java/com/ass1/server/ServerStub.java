package com.ass1.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.rmi.AlreadyBoundException;
import com.ass1.*;

public class ServerStub {
	Server server;
	Identifier zoneId;
	ServerStub stub;

	public ServerStub(Server server, Identifier zone) throws RemoteException {
		this.zoneId = zone;
		this.server = server;
		this.registerToProxyServer();
	}

	private void registerToProxyServer() throws RemoteException {
		System.out.println("Connecting to ProxyServer");
		Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);

		ServerInterface srv = (ServerInterface) UnicastRemoteObject.exportObject(this.server, 0);

		String serverRegister = this.getRegistryName();
		try {
			registry.bind(serverRegister, srv);
		} catch (AlreadyBoundException e) {
			registry.rebind(serverRegister, srv);
		}

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
