package com.kenzie.marketing.referral.service.caching;

import com.kenzie.marketing.referral.service.dependency.DaggerServiceComponent;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import java.util.Optional;

public class CacheClient {

    public CacheClient() {}

    // Put your Cache Client Here

    // Since Jedis is being used multithreaded, you MUST get a new Jedis instances and close it inside every method.
    // Do NOT use a single instance across multiple of these methods

    // Use Jedis in each method by doing the following:
    // Jedis cache = DaggerServiceComponent.create().provideJedis();
    // ... use the cache
    // cache.close();

    // Remember to check for null keys!

    public void setValue(String key, int seconds, String value) {
        // Check for non-null key
        // Set the value in the cache
        if (key == null) {
            throw new IllegalArgumentException();
        }
        Jedis cache = DaggerServiceComponent.create().provideJedis();

        cache.setex(key, seconds, value);

        cache.close();
    }

    public Optional<String> getValue(String key) {
        // Check for non-null key
        // Retrieves the Optional values from the cache
        if (key == null) {
            throw new IllegalArgumentException();
        }
        Jedis cache = DaggerServiceComponent.create().provideJedis();
        Optional<String> valueReturn = Optional.ofNullable(cache.get(key));
        cache.close();
            return valueReturn;
    }

    public void invalidate(String key) {
        // Check for non-null key
        // Delete the key
        // Might be issue here
        if (key == null) {
            throw new IllegalArgumentException();
        }
        Jedis cache = DaggerServiceComponent.create().provideJedis();

            cache.del(key);
            cache.close();
    }

//    private void checkNonNullKey(String key) {
//        // Ensure the key isn't null
//        // What should you do if the key *is* null?
//    }
}
