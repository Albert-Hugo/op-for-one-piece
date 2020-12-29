package com.ido.op.chopper;

import java.util.Map;

/**
 * @author Carl
 * @date 2019/7/12
 */
public class LocalCacheManager implements ChopperCacheManager {

    private CacheMap<Object, Object> cacheTable = new CacheMap<>();

    @Override
    public Object get(String k) {
        return cacheTable.get(k);
    }

    @Override
    public Object put(String k, Object v, long expireTime) {
        return cacheTable.put(k, v, expireTime);
    }

    @Override
    public void expire(String kPrefix) {
        for (Map.Entry entry : cacheTable.entrySet()) {
            String k = (String) entry.getKey();
            if (k.startsWith(kPrefix)) {
                cacheTable.expire(k);
            }
        }
    }
}
