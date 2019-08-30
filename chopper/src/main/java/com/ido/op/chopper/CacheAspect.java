package com.ido.op.chopper;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Aspect
@Component
public class CacheAspect {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private ChopperCacheManager chopperCacheManager;
    private KeyStrategy keyStrategy;

    public CacheAspect(ChopperCacheManager chopperCacheManager) {
        this.chopperCacheManager = chopperCacheManager;
    }

    @Pointcut("@annotation(com.ido.op.chopper.Cacheable)")
    private void Intercepter() {
    }

    @Around(value = "Intercepter()")
    public Object cache(ProceedingJoinPoint joinpoint) throws Throwable {
        Method method = ((MethodSignature) joinpoint.getSignature()).getMethod();

        Annotation[] annotations = method.getDeclaredAnnotations();
        Object[] args = joinpoint.getArgs();
        if (annotations != null && annotations.length > 0) {

            for (Annotation a : annotations) {
                if (a instanceof Cacheable) {
                    Cacheable ca = ((Cacheable) a);
                    if (ca.key().length() == 0 && keyStrategy == null) {
                        log.warn(" cache key strategy not found {} ", ca.keyStrategy().getName());
                        return joinpoint.proceed();
                    }

                    String key;
                    if (ca.key().length() == 0) {
                        key = keyStrategy.getKey(joinpoint.getTarget(), method, args);
                    } else {
                        key = ca.key();
                    }
                    final String ck = ca.keyPrefix() + key;
                    Object cacheResult = chopperCacheManager.get(ck);
                    if (cacheResult != null) {
                        if (log.isDebugEnabled()) {
                            log.debug(" get result from cache , class {}, key {}", method.getDeclaringClass().getName(), key);
                        }
                        return cacheResult;
                    }

                    Object rtv = joinpoint.proceed();
                    long expiredTime = ca.expireTime();
                    chopperCacheManager.put(ck, rtv, expiredTime);
                    return rtv;


                }
            }
        }

        return joinpoint.proceed();
    }


    @Autowired
    public void setKeyStrategy(KeyStrategy keyStrategy) {
        this.keyStrategy = keyStrategy;
    }
}
