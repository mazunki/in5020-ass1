package com.ass1.client;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.ass1.server.ServerInterface;

public class Client {
	public static void main(String[] args) {
		try {
			Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1234);
			ServerInterface server = (ServerInterface) registry.lookup("server");
			System.out.println("asking server...");
			System.out.println("done");
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
}
