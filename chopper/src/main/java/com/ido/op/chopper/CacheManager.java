package com.ido.op.chopper;

/**
 * @author Carl
 * @date 2019/7/12
 */
public interface CacheManager {

    Object get(Object k);

    Object put(Object k, Object v, long expireTime);
}
