package com.ass1;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class QueryResultCache {

    public static final int DEFAULT_SERVER_CACHE_LIMIT = 150;
    public static final int DEFAULT_CLIENT_CACHE_LIMIT = 45;
    int size;
    LinkedHashMap<String, Object> cache;

    public QueryResultCache(int size) {
        QueryResultCache self = this;
        this.size = size;
        this.cache = new LinkedHashMap<String, Object>(this.size, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
                boolean should_del = size() > self.size;
                if (should_del) {
                    System.out.println("brr from cache");
                }
                return size() > self.size;
            }
        };
    }

    public boolean has(String method, Object[] args) {
        return this.cache.containsKey(this.cacheKeyGenerator(method, args));
    }
    public boolean has(Method method, Object[] args) {
        return this.cache.containsKey(this.cacheKeyGenerator(method, args));
    }
    public <T> T get(String method, Object[] args) {
        String key = this.cacheKeyGenerator(method, args);
        Object result = this.cache.get(key);
        System.out.println("[cache] restoring " + key + " as " + result);

        return (T) result;
    }
    public <T> T get(Method method, Object[] args) {
        String key = this.cacheKeyGenerator(method, args);
        Object result = this.cache.get(key);
        System.out.println("[cache] restoring " + key + " as " + result);

        return (T) result;
    }

    public Object remember(Method method, Object[] args, Object value) {
        String key = this.cacheKeyGenerator(method, args);
        System.out.println("[cache] remembering " + key + " as " + value);
        this.cache.put(key, value);
        return value;
    }

    public Object remember(String method, Object[] args, Object value) {
        String key = this.cacheKeyGenerator(method, args);
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
