package com.ido.op.chopper;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.ido.op.chopper.Cacheable.NEVER_EXPIRE;

/**
 * @author Carl
 * @date 2019/7/11
 */
public class CacheMap<K, V> {

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


    public Set<Entry<K, MetaData<V>>> entrySet() {
        return map.entrySet();
    }

    interface RemoveListener<K, V> {
        void onRemove(K k, V v);
    }


    private class MetaData<V> {
        long expireTime;
        V value;

        MetaData(V value, long expireTime) {
            this.expireTime = expireTime;
            this.value = value;
        }

        boolean isExpire() {
            if (expireTime == NEVER_EXPIRE) {
                return false;
            }
            return System.currentTimeMillis() > expireTime;
        }


    }

    public void expire(String k) {
        MetaData<V> md = map.get(k);
        md.expireTime = -2;
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
        MetaData<V> previousValue;
        if (expireTime == NEVER_EXPIRE) {
            previousValue = map.put(key, new MetaData<>(value, NEVER_EXPIRE));
        } else {
            previousValue = map.put(key, new MetaData<>(value, System.currentTimeMillis() + NEVER_EXPIRE));
        }
        return previousValue == null ? null : previousValue.value;
    }


}
