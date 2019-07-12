package com.ido.op.chopper;

import java.lang.reflect.Method;

public interface KeyStrategy {
    /**
     * @param target the target object to call
     * @param method the target method
     * @param params the parameters
     * @return the key
     */
    String getKey(Object target, Method method, Object[] params);
}
