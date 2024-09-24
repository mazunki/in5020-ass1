package com.ass1.server;

import java.rmi.RemoteException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ass1.loadbalancer.*;

public class SimulateServer {
	public static void main(String[] args) throws RemoteException, InterruptedException {
		ProxyServer proxyserver = new ProxyServer(1099);

		ServerStub[] servers = {
				new ServerStub("norway", "1"),
				new ServerStub("egypt", "2"),
				new ServerStub("canada", "3"),
				new ServerStub("japan", "4"),
				new ServerStub("australia", "5"),
		};

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(servers.length + 1);

		executor.submit(() -> {
			proxyserver.start();
		});

		Thread.sleep(500);

		for (ServerStub server : servers) {
			executor.submit(() -> {
				try {
					server.launch();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			});
			Thread.sleep(20);
		}

		executor.shutdown();

		while (!executor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
		}
	}
}
