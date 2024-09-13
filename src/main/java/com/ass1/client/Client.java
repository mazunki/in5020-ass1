package com.ass1.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

import com.ass1.server.ServerInterface;
import com.ass1.*;
import com.ass1.loadbalancer.ProxyServerInterface;

public class Client {
	final static String PROXY_SERVER = "127.0.0.1";
	final static int PROXY_PORT = 1099;
	Identifier zoneId;

	Registry proxyRegistry;
	ProxyServerInterface proxyServer;
	ServerInterface server;

	public Client(String zoneId) {
		this.zoneId = new Identifier(zoneId);

		try {
			this.proxyRegistry = LocateRegistry.getRegistry(PROXY_SERVER, PROXY_PORT);
		} catch (RemoteException e) {
			throw new RuntimeException("Could not connect to proxy server... 😅");
		}

		try {
			this.proxyServer = (ProxyServerInterface) this.proxyRegistry
					.lookup(ProxyServerInterface.PROXY_IDENTIFIER);
		} catch (RemoteException e) {
			throw new RuntimeException("Failed to connect with proxy server! 😷");
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

	// Method getMethod(String name, int argcount) throws NoSuchMethodException {
	// for (Method m : this.server.getClass().getMethods()) {
	// if (m.getName().equals(name) && m.getParameterCount() == argcount) {
	// return m;
	// }
	// }
	// throw new NoSuchMethodException("No such method: '" + name + "' on server");
	// }

	public Object makeQuery(String method, String[] args) {
		Method callable;

		try {
			callable = server.getClass().getMethod(method, String[].class);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("No such method: '" + method + "'");
		}

		Object result;
		try {
			result = callable.invoke(this.server, (Object) args);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("You are not allowed to run this method! 😡");
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Failed to invoke method...");
		}

		return result;
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
		String zoneId = args[args.length - 1];
		String method = args[0];
		String[] query_args = Arrays.copyOfRange(args, 1, args.length - 1);

		Client client = new Client(zoneId);
		Object response = client.makeQuery(method, query_args);
		System.out.println(response);
	}
}
