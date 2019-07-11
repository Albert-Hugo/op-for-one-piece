package com.ido.op.chopper;

public interface KeyStrategy {
    /**
     * the parameters to generate cache key
     *
     * @param params
     * @return
     */
    String getKey(Object[] params);
}
