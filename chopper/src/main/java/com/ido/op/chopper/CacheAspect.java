package com.ido.op.chopper;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Aspect
@Component
public class CacheAspect implements ApplicationContextAware {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private ApplicationContext applicationContext;

    @Autowired
    private CacheManager cacheManager;

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
                    KeyStrategy keyStrategy;
                    try {
                        keyStrategy = (KeyStrategy) applicationContext.getBean(ca.keyStrategy());
                    } catch (NoSuchBeanDefinitionException e) {
                        log.warn(" cache key strategy not found {} ", ca.keyStrategy().getName());
                        return joinpoint.proceed();
                    }
                    String key;
                    if (ca.key().length() == 0) {
                        key = keyStrategy.getKey(joinpoint.getTarget(), method, args);
                    } else {
                        key = ca.key();
                    }
                    Object cacheResult = cacheManager.get(key);
                    if (cacheResult != null) {
                        if (log.isDebugEnabled()) {
                            log.debug(" get result from cache , class {}, key {}", method.getDeclaringClass().getName(), key);
                        }
                        return cacheResult;
                    }

                    Object rtv = joinpoint.proceed();
                    long expiredTime = ca.expireTime();
                    cacheManager.put(key, rtv, expiredTime);
                    return rtv;


                }
            }
        }

        return joinpoint.proceed();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
