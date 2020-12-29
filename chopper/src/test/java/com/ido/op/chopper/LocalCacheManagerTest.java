package com.ido.op.chopper;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Ido
 * @date 2020/12/29 9:14
 */
public class LocalCacheManagerTest {

    @Test
    public void testExpire(){

        CacheMap cacheMap = new CacheMap();

        cacheMap.put("fsfd","get",100);
        Assert.assertNotNull(cacheMap.get("fsfd"));;
        cacheMap.expire("fsfd");
        Assert.assertNull(cacheMap.get("fsfd"));;

    }
}
