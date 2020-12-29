package com.ido.op.chopper;

/**
 * @author Carl
 * @date 2019/7/12
 */
public interface ChopperCacheManager {

    Object get(String k);

    Object put(String k, Object v, long expireTime);

    /**
     * 使前缀为 kPrefix 的数据过期
     *
     * @param kPrefix
     */
    void expire(String kPrefix);
}
