package com.ido.luffy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Carl
 * @date 2019/6/14
 */
@Configuration
public class LuffyAutoConfig {

    @Bean
    @ConditionalOnMissingBean()
    public SecurityManager securityManager() {

        SecurityManager securityManager = new SecurityManager<>(new MemoryPermissionRepo(LuffyConfig.rolesUrlTable));

        return securityManager;


    }
}
