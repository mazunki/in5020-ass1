
package com.ass1.server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.ass1.loadbalancer.ProxyServerInterface;

public class StopProxyServer {
	final static String PROXY_SERVER = "127.0.0.1";
	final static int PROXY_PORT = 1099;

	private static ProxyServerInterface proxyServer;
	private static Registry proxyRegistry;

	public static void main(String[] args) {
		try {
			StopProxyServer.proxyRegistry = LocateRegistry.getRegistry(StopProxyServer.PROXY_SERVER,
					StopProxyServer.PROXY_PORT);
		} catch (RemoteException e) {
			throw new RuntimeException("Could not connect to proxy server... 😅");
		}

		try {
			StopProxyServer.proxyServer = (ProxyServerInterface) StopProxyServer.proxyRegistry
					.lookup(ProxyServerInterface.PROXY_IDENTIFIER);
		} catch (RemoteException e) {
			throw new RuntimeException("Failed to connect with proxy server! 😷\n" + e.getMessage());
		} catch (NotBoundException e) {
			throw new RuntimeException("Failed to find proxy server reference...");
		}

		try {
			proxyServer.stop();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
