package com.ass1.server;

import java.rmi.RemoteException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ass1.loadbalancer.*;

public class SimulateServer {
	public static void main(String[] args) throws RemoteException {
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

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			return;
		}

		for (ServerStub server : servers) {
			executor.submit(() -> {
				try {
					server.launch();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			});
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				System.err.println("Was submitting tasks...");
			}
		}

		executor.shutdown();

		try {
			while (!executor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
			}
		} catch (InterruptedException e) {
			System.out.println("Okay bye");
			proxyserver.stop();
		}
	}
}
