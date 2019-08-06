package com.ido.op.chopper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Carl
 * @date 2019/7/11
 */
public class CacheMap<K, V> {
    private static final long NEVER_EXPIRE = 0;

    private ConcurrentMap<K, MetaData<V>> map = new ConcurrentHashMap<>();
    /**
     * millisecond base
     */
    private RemoveListener<K, V> removeListener;

    public CacheMap() {
        this((RemoveListener) (o, o2) -> {
        });
    }

    public CacheMap(RemoveListener<K, V> listener) {
        this.removeListener = listener;
    }

    interface RemoveListener<K, V> {
        void onRemove(K k, V v);
    }


    private class MetaData<V> {
        long storeTime;
        long expireTime;
        V value;

        MetaData(V value, long storeTime, long expireTime) {
            this.storeTime = storeTime;
            this.expireTime = expireTime;
            this.value = value;
        }

        boolean isExpire() {
            if (expireTime == NEVER_EXPIRE) {
                return false;
            }
            return (System.currentTimeMillis()/1000 - this.storeTime) > expireTime;
        }


    }

    public V get(K key) {
        MetaData<V> md = map.get(key);
        if (md == null) {
            return null;
        }
        if (md.isExpire()) {
            map.remove(key);

            removeListener.onRemove(key, md.value);
            return null;
        }
        return md.value;
    }

    /**
     * 0 means never expire
     *
     * @param key
     * @param value
     * @param expireTime the expire time
     * @return
     */
    public V put(K key, V value, long expireTime) {
        MetaData<V> previousValue = map.put(key, new MetaData<>(value, System.currentTimeMillis()/1000, expireTime));
        return previousValue == null ? null : previousValue.value;
    }


}
