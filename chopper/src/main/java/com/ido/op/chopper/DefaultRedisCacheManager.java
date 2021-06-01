package com.ido.op.chopper;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.ido.op.chopper.Cacheable.NEVER_EXPIRE;

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
        if (expireTime == NEVER_EXPIRE) {
            redisTemplate.opsForValue().set(k, v);
        } else {
            redisTemplate.opsForValue().set(k, v, expireTime, TimeUnit.SECONDS);
        }
    }

    @Override
    public void expire(final String keyPattern) {
        redisTemplate.execute((RedisCallback) connection -> {
            Set<byte[]> keys = connection.keys(keyPattern.getBytes());
            for (byte[] bs : keys) {
                redisTemplate.expire(new String(bs), 0, TimeUnit.SECONDS);
            }
            return null;
        });
    }
}
