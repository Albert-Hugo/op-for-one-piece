package com.ido.op.chopper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Carl
 * @date 2019/7/12
 */
@Configuration
public class MemCacheAutoConfig {

    @Bean
    @ConditionalOnMissingBean()
    public KeyStrategy keyStrategy() {
        return new AllParameterKeyStrategy();


    }


    @Bean
    @ConditionalOnMissingBean()
    public CacheManager cacheManager() {
        return new LocalCacheManager();


    }

}
