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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Aspect
@Component
public class CacheAspect implements ApplicationContextAware {
   private  Logger log = LoggerFactory.getLogger(this.getClass());
    private ApplicationContext applicationContext;
    private ConcurrentMap<Class, CacheMap<String, Object>> cacheTable = new ConcurrentHashMap<>();

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
                    String prefix = ca.keyPrefix();


                    if (StringUtils.isEmpty(prefix)) {
                        prefix = method.getName();
                    }
                    KeyStrategy keyStrategy;
                    try {
                        keyStrategy = (KeyStrategy) applicationContext.getBean(ca.keyStrategy());
                    } catch (NoSuchBeanDefinitionException e) {
                        log.warn(" cache key strategy not found {} ", ca.keyStrategy().getName());
                        return joinpoint.proceed();
                    }
                    String key ;
                    if(ca.key().length() == 0){
                        key = prefix + ":" + keyStrategy.getKey(args);
                    }else {
                        key = ca.key();
                    }

                    CacheMap<String, Object> clxTable = cacheTable.get(method.getDeclaringClass());
                    if (clxTable != null) {
                        Object cacheResult = clxTable.get(key);
                        if (cacheResult != null) {
                            if (log.isDebugEnabled()) {
                                log.debug(" get result from cache , class {}, key {}", method.getDeclaringClass().getName(), key);
                            }
                            return cacheResult;

                        }
                        Object rtv = joinpoint.proceed();
                        long expiredTime = ca.expireTime();
                        clxTable.put(key, rtv,expiredTime);

                        return rtv;


                    }else{
                        Object rtv = joinpoint.proceed();
                        long expiredTime = ca.expireTime();
                        clxTable = new CacheMap<>();
                        clxTable.put(key, rtv,expiredTime);
                        cacheTable.put(method.getDeclaringClass(), clxTable);
                        return rtv;
                    }


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
