package com.ido.op.chopper;

/**
 * @author Carl
 * @date 2019/7/12
 */
public interface ChopperCacheManager {

    Object get(String k);

    void put(String k, Object v, long expireTime);

    /**
     * 使 符合 pattern 的数据过期
     *
     * @param keyPattern
     */
    void expire(String keyPattern);
}
