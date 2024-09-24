package com.ass1.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ass1.LoggerUtil;
import com.ass1.loadbalancer.ProxyServerInterface;

public class SimulateClient {
	private static final Logger logger = LoggerUtil.createLogger(SimulateClient.class.getName(), "client", "sim",
			Level.CONFIG);

	private static Integer MAX_QUERIES = null;
	private static int DELAY_QUERIES_1 = 50; // ms
	private static int DELAY_QUERIES_2 = 20; // ms
	private static List<ClientTask> tasks = new ArrayList<>();

	private static ProxyServerInterface proxyServer;
	private static Registry proxyRegistry;

	public static void main(String[] args) throws InterruptedException {

		logger.info("Starting SimulateClient");

		// String inputFile = "src/main/resources/exercise_1_input.txt";
		String inputFile = "src/main/resources/exercise_2_input.txt";

		Pattern pattern = Pattern.compile("(\\w+)\\s+(.*?)\\s*Zone:(\\d+)"); // <method> [<args...> ]Zone:<zone>
		Pattern argsPattern = Pattern.compile("(\\d+|[\\w&&[^\\d]]+(?:\s+[\\w&&[^\\d]]+)*)"); // <<number>|<multiword>...>
		Matcher matcher, argsMatcher;

		try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
			int lineno = 0;
			String line;
			while ((line = br.readLine()) != null) {
				lineno++;
				matcher = pattern.matcher(line);

				if (!matcher.matches()) {
					logger.warning("Invalid query on line: " + lineno + ": " + line);
					continue;
				}

				String method = matcher.group(1);
				ArrayList<String> arguments = new ArrayList<String>();
				argsMatcher = argsPattern.matcher(matcher.group(2));
				while (argsMatcher.find()) {
					arguments.add(argsMatcher.group().trim());
				}

				String zoneId = matcher.group(3);

				ClientTask task = new ClientTask(zoneId, method, arguments.toArray(new String[0]),
						lineno);
				tasks.add(task);

				if (MAX_QUERIES != null && tasks.size() >= MAX_QUERIES) {
					break;
				}
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error reading input file: " + e.getMessage());
		}
		logger.info("Parsed a total of " + tasks.size() + " tasks");

		try {
			SimulateClient.proxyRegistry = LocateRegistry.getRegistry(Client.PROXY_SERVER,
					Client.PROXY_PORT);
		} catch (RemoteException e) {
			throw new RuntimeException("Could not connect to proxy server... 😅");
		}

		try {
			SimulateClient.proxyServer = (ProxyServerInterface) SimulateClient.proxyRegistry
					.lookup(ProxyServerInterface.PROXY_IDENTIFIER);
		} catch (RemoteException e) {
			throw new RuntimeException("Failed to connect with proxy server! 😷" + e.getMessage());
		} catch (NotBoundException e) {
			throw new RuntimeException("Failed to find proxy server reference...");
		}

		SimulateClient.start_tasks(DELAY_QUERIES_1);
		SimulateClient.start_tasks(DELAY_QUERIES_2);

		try {
			SimulateClient.proxyServer.stop();
		} catch (RemoteException e) {
			logger.severe("Couldn't terminate sesssion");
		}

		logger.info("All simulations complete");
	}

	public static void start_tasks(int delay) throws InterruptedException {
		int nproc = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(nproc - 1);
		logger.config("Using " + (nproc - 1) + " processors");

		logger.info("Submitting tasks with a delay of " + delay + "ms");

		// for (ClientTask task : tasks.subList(792, 850)) {
		for (ClientTask task : tasks) {
			executor.submit(task);
			Thread.sleep(delay);
		}

		logger.info("All tasks submitted, awaiting termination");

		executor.shutdown(); // stop accepting tasks
		while (!executor.awaitTermination(50, TimeUnit.MILLISECONDS)) {
			// spin until the already-accepted tasks are done.
		}

		for (Entry<String, List<Long>> entry : ClientTask.meter_turnaround.entrySet()) {
			double avg = entry.getValue().stream().mapToDouble(Long::longValue).average().orElse(0.0);
			double min = entry.getValue().stream().mapToDouble(Long::longValue).min().orElse(0.0);
			double max = entry.getValue().stream().mapToDouble(Long::longValue).max().orElse(0.0);
			logger.info(entry.getKey() +
					" turn-around: avg " + avg + "ms, min " + min + "ms, max " + max + "ms");
		}

		logger.info("Ending simulation");
	}

	static class ClientTask implements Runnable {
		private Client client;
		private final String zoneId;
		private final String method;
		private final String[] arguments;
		private final int lineno;
		private static int counter = 0;

		private static HashMap<String, List<Long>> meter_turnaround = new HashMap<String, List<Long>>();

		public ClientTask(String zoneId, String method, String[] arguments, int lineno) {
			this.zoneId = zoneId;
			this.method = method;
			this.arguments = arguments;
			this.lineno = lineno;
		}

		@Override
		public void run() {
			try {
				this.client = new Client(this.zoneId, true);
				long start_time = System.currentTimeMillis();
				Object result = client.makeQuery(method, arguments);

				ClientTask.addTurnaround(this.method, start_time, System.currentTimeMillis());

				logger.finer(this + "... " + result);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Failed to execute " + this + ". " + e.getMessage());
			}

			counter++;
			if (counter % 300 == 0) {
				logger.info("Completed " + counter + " tasks.");
			}
		}

		public static void addTurnaround(String method, long start, long end) {
			long duration = end - start;
			List<Long> avgs = ClientTask.meter_turnaround.getOrDefault(method, new ArrayList<>());
			avgs.add(duration);
			ClientTask.meter_turnaround.put(method, avgs);
		}

		public String toString() {
			return "#" + this.lineno + ":" + this.method + ":{" + String.join(";", this.arguments)
					+ "}@zone-" + zoneId;

		}

	}
}
