package play.cache;

import java.util.*;

/**
 * This delegate class is necessary because you can not inherit static methods, so the only
 * option you have when it comes to extend static java classes is to create a delegate around them,
 * then use type either a type alias or a val
 */
@Deprecated
abstract class CacheDelegate {

    public void add(String key, Object value, String expiration) {
        Cache.add(key, value, expiration);
    }

    public boolean safeAdd(String key, Object value, String expiration) {
        return Cache.safeAdd(key, value, expiration);
    }

    public void add(String key, Object value) {
        Cache.add(key, value);
    }

    public void set(String key, Object value, String expiration) {
        Cache.set(key, value, expiration);
    }

    public boolean safeSet(String key, Object value, String expiration) {
        return Cache.safeSet(key, value, expiration);
    }

    public void set(String key, Object value) {
        Cache.set(key, value);
    }

    public void replace(String key, Object value, String expiration) {
        Cache.replace(key, value, expiration);
    }

    public boolean safeReplace(String key, Object value, String expiration) {
        return Cache.safeReplace(key, value, expiration);
    }

    public void replace(String key, Object value) {
        Cache.replace(key, value);
    }

    public long incr(String key, int by) {
        return Cache.incr(key, by);
    }

    public long incr(String key) {
        return Cache.incr(key);
    }

    public long decr(String key, int by) {
        return Cache.decr(key, by);
    }

    public long decr(String key) {
        return Cache.decr(key);
    }

    public Map<String, Object> get(String... key) {
        return Cache.get(key);
    }

    public void delete(String key) {
        Cache.delete(key);
    }

    public boolean safeDelete(String key) {
        return Cache.safeDelete(key);
    }

    public void clear() {
        Cache.clear();
    }

    CacheImpl _impl() {
        return Cache.cacheImpl;
    }

}

