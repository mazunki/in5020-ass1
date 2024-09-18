package com.ass1;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class QueryResultCache {

    public static final int DEFAULT_SERVER_CACHE_LIMIT = 150;
    public static final int DEFAULT_CLIENT_CACHE_LIMIT = 10;
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
                    System.out.println("brr from cache");
                }
                return should_del;
            }
        };
    }

    public boolean has(String method, Object[] args) {
        String key = QueryResultCache.cacheKeyGenerator(method, args);
        boolean found = this.cache.containsKey(key);
        if (found) {
            System.out.println("[cache] has key: " + key);
        }
        return found;
    }
    public boolean has(Method method, Object[] args) {
        String key = QueryResultCache.cacheKeyGenerator(method, args);
        boolean found = this.cache.containsKey(key);
        if (found) {
            System.out.println("[cache] has key: " + key);
        }
        return found;
    }
    public Object get(String method, Object[] args) {
        String key = QueryResultCache.cacheKeyGenerator(method, args);
        Object result = this.cache.get(key);
        System.out.println("[cache] restoring " + key + " as " + result);

        return result;
    }
    public Object get(Method method, Object[] args) {
        String key = QueryResultCache.cacheKeyGenerator(method, args);
        Object result = this.cache.get(key);
        System.out.println("[cache] restoring " + key + " as " + result);

        return result;
    }

    public Object remember(Method method, Object[] args, Object value) {
        String key = QueryResultCache.cacheKeyGenerator(method, args);
        System.out.println("[cache] remembering " + key + " as " + value);
        this.cache.put(key, value);
        return value;
    }

    public Object remember(String method, Object[] args, Object value) {
        String key = QueryResultCache.cacheKeyGenerator(method, args);
        System.out.println("[cache] remembering " + key + " as " + value);
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
