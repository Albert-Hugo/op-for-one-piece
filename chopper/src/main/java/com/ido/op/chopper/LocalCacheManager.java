package com.ido.op.chopper;

/**
 * @author Carl
 * @date 2019/7/12
 */
public class LocalCacheManager implements ChopperCacheManager {

    private CacheMap<Object, Object> cacheTable = new CacheMap<>();

    @Override
    public Object get(Object k) {
        return cacheTable.get(k);
    }

    @Override
    public Object put(Object k, Object v, long expireTime) {
        return cacheTable.put(k, v, expireTime);
    }
}
