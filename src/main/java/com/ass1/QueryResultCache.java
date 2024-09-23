package com.ass1;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class QueryResultCache {

	public static final int DEFAULT_SERVER_CACHE_LIMIT = 150;
	public static final int DEFAULT_CLIENT_CACHE_LIMIT = 10;

	private final Logger logger;

	int size;
	LinkedHashMap<String, Object> cache;

	AtomicInteger hit = new AtomicInteger(0);
	AtomicInteger miss = new AtomicInteger(0);
	AtomicInteger cache_lookups = new AtomicInteger(0);
	private static int HIT_OR_MISS_REPORT_INTERVAL = 10;

	public QueryResultCache(int size) {
		this(size, "cache");
	}

	public QueryResultCache(int size, String identifier) {
		this(size, LoggerUtil.createLogger(QueryResultCache.class.getName() + "_" + identifier, "cache",
				"#-" + identifier));
	}

	public QueryResultCache(int size, Logger logger, String identifier) {
		this(size, LoggerUtil.deriveLogger(logger, "cache-" + identifier));
	}

	public QueryResultCache(int size, Logger logger) {
		QueryResultCache self = this;
		this.logger = logger;

		this.size = size;
		this.cache = new LinkedHashMap<String, Object>(this.size) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
				boolean should_del = size() > self.size;
				if (should_del) {
					logger.fine("Removed item from cache");
				} else {
					logger.finer("Did not remove item from cache");
				}
				return should_del;
			}
		};
		logger.config("Cache is ready!");
	}

	public boolean has(String method, Object[] args) {
		String key = QueryResultCache.cacheKeyGenerator(method, args);
		boolean found = this.cache.containsKey(key);
		if (found) {
			hit.incrementAndGet();
			logger.finest("Found key: " + key);
		} else {
			miss.incrementAndGet();
			logger.finer("Did NOT find key: " + key);
		}

		if (cache_lookups.incrementAndGet() % HIT_OR_MISS_REPORT_INTERVAL == 0) {
			logger.info("Cache hit rate: " + hit.get() + "/" + cache_lookups.get());
		}
		return found;
	}

	public boolean has(Method method, Object[] args) {
		return has(method.getName(), args);
	}

	public Object get(String method, Object[] args) {
		String key = QueryResultCache.cacheKeyGenerator(method, args);
		Object result = this.cache.get(key);
		logger.fine("Restoring " + key + " as " + result);

		return result;
	}

	public Object get(Method method, Object[] args) {
		return get(method.getName(), args);
	}

	public Object remember(String method, Object[] args, Object value) {
		String key = QueryResultCache.cacheKeyGenerator(method, args);
		logger.finer("Remembering " + key + " as " + value);
		this.cache.put(key, value);
		return value;
	}

	public Object remember(Method method, Object[] args, Object value) {
		return remember(method.getName(), args, value);
	}

	public static String cacheKeyGenerator(Method method, Object[] args) {
		return method + ":" + Arrays.deepHashCode(args);
	}

	public static String cacheKeyGenerator(String method, Object[] args) {
		return method + ":" + Arrays.deepHashCode(args);
	}
}
