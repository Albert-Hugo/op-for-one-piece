package com.ido.op.chopper;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AllParameterKeyStrategy implements KeyStrategy {
    public String getKey(Object[] params) {
        StringBuilder key = new StringBuilder();
        for (Object o : params) {
            key.append(o.toString()).append(":");
        }
        return key.toString();
    }


}
