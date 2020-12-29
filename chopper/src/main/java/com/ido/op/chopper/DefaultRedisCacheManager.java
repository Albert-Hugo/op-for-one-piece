package com.ido.op.chopper;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Ido
 * @date 2020/12/29 10:47
 */
public class DefaultRedisCacheManager implements ChopperCacheManager {
    private RedisTemplate redisTemplate;

    public DefaultRedisCacheManager(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Object get(String k) {
        return redisTemplate.opsForValue().get(k);
    }

    @Override
    public void put(String k, Object v, long expireTime) {
        redisTemplate.opsForValue().set(k, v, expireTime, TimeUnit.SECONDS);
    }

    @Override
    public void expire(String kPrefix) {
        String kPattern = kPrefix + "*";
        redisTemplate.execute((RedisCallback) connection -> {
            Set<byte[]> keys = connection.keys(kPattern.getBytes());
            for (byte[] bs : keys) {
                redisTemplate.expire(new String(bs), 0, TimeUnit.SECONDS);
            }
            return null;
        });
    }
}
