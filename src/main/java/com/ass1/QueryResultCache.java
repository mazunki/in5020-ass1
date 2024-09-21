package com.ass1;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueryResultCache {

	public static final int DEFAULT_SERVER_CACHE_LIMIT = 150;
	public static final int DEFAULT_CLIENT_CACHE_LIMIT = 10;

	private static final Logger logger = Logger.getLogger(QueryResultCache.class.getName());
	private static final Level logLevel = Level.INFO;
	private static final OnelineFormatter fmt = new OnelineFormatter("cache");
	private static final ConsoleHandler consHandler = new ConsoleHandler();
	private static FileHandler fileHandler;
	int size;
	LinkedHashMap<String, Object> cache;

	public QueryResultCache(int size) {
		QueryResultCache self = this;
		this.size = size;
		this.cache = new LinkedHashMap<String, Object>(this.size) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
				boolean should_del = size() > self.size;
				if (should_del) {
					logger.finer("removed item from cache");
				} else {
					logger.finer("did not remove item from cache");
				}
				return should_del;
			}
		};
		if (fileHandler == null) {
			try {
				fileHandler = new FileHandler("log/server.log", true);
				fileHandler.setFormatter(fmt);
				logger.addHandler(fileHandler);

				consHandler.setFormatter(fmt);
				logger.addHandler(consHandler);
				logger.setUseParentHandlers(false);

				logger.setLevel(QueryResultCache.logLevel);
			} catch (IOException e) {
				System.err.println("Failed to initialize logger: " + e.getMessage());
			}
		}
	}

	public boolean has(String method, Object[] args) {
		String key = QueryResultCache.cacheKeyGenerator(method, args);
		boolean found = this.cache.containsKey(key);
		if (found) {
			logger.finer("found key: " + key);
		}
		return found;
	}

	public boolean has(Method method, Object[] args) {
		String key = QueryResultCache.cacheKeyGenerator(method, args);
		boolean found = this.cache.containsKey(key);
		if (found) {
			logger.finer("found key: " + key);
		}
		return found;
	}

	public Object get(String method, Object[] args) {
		String key = QueryResultCache.cacheKeyGenerator(method, args);
		Object result = this.cache.get(key);
		logger.info("restoring " + key + " as " + result);

		return result;
	}

	public Object get(Method method, Object[] args) {
		String key = QueryResultCache.cacheKeyGenerator(method, args);
		Object result = this.cache.get(key);
		logger.info("restoring " + key + " as " + result);

		return result;
	}

	public Object remember(Method method, Object[] args, Object value) {
		String key = QueryResultCache.cacheKeyGenerator(method, args);
		logger.fine("[cache] remembering " + key + " as " + value);
		this.cache.put(key, value);
		return value;
	}

	public Object remember(String method, Object[] args, Object value) {
		String key = QueryResultCache.cacheKeyGenerator(method, args);
		logger.fine("[cache] remembering " + key + " as " + value);
		this.cache.put(key, value);
		return value;
	}

	public static String cacheKeyGenerator(Method method, Object[] args) {
		return method + ":" + Arrays.deepHashCode(args);
	}

	public static String cacheKeyGenerator(String method, Object[] args) {
		return method + ":" + Arrays.deepHashCode(args);
	}
}
