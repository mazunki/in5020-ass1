package com.ass1.server;

import java.rmi.RemoteException;
import com.ass1.loadbalancer.*;

public class SimulateServer {
	public static void main(String[] args) throws RemoteException, InterruptedException {
		ProxyServer proxyserver = new ProxyServer(1099);

		Server[] servers = {
				new Server("norway", "1"),
				new Server("egypt", "2"),
				new Server("canada", "3"),
				new Server("japan", "4"),
				new Server("australia", "5"),
		};

		proxyserver.start();
		Thread.sleep(500);

		for (Server server : servers) {
			server.launch();
			Thread.sleep(20);
		}

	}
}
