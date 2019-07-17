package com.ido.op.chopper;

import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

@Configuration
public class AllParameterKeyStrategy implements KeyStrategy {
    @Override
    public String getKey(Object target, Method method, Object[] params) {
        StringBuilder key = new StringBuilder();
        key.append(target.getClass().getName())
                .append(":")
                .append(method.getName())
                .append(":");
        for (Object o : params) {
            key.append(o!=null?o.toString():"null").append(":");
        }
        return key.toString();
    }


}
