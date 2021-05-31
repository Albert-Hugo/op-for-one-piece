package com.ido.op.chopper;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.data.mapping.model.DefaultSpELExpressionEvaluator;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Aspect
@Component
@Order
public class CacheAspect implements ApplicationContextAware {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private ChopperCacheManager chopperCacheManager;
    private KeyStrategy keyStrategy;
    private ApplicationContext applicationContext;

    SpelExpressionParser parser = new SpelExpressionParser();


    public CacheAspect(ChopperCacheManager chopperCacheManager) {
        this.chopperCacheManager = chopperCacheManager;
    }

    @Pointcut("@annotation(com.ido.op.chopper.Cacheable)")
    private void IntercepterCacheable() {
    }

    @Pointcut("@annotation(com.ido.op.chopper.CacheExpire)")
    private void IntercepterCacheExpire() {
    }

    @Around(value = "IntercepterCacheExpire()")
    public Object expire(ProceedingJoinPoint joinpoint) throws Throwable {
        Method method = ((MethodSignature) joinpoint.getSignature()).getMethod();

        Annotation[] annotations = method.getDeclaredAnnotations();
        String expression = "";
        String keyPattern = "";
        if (annotations != null && annotations.length > 0) {

            for (Annotation a : annotations) {
                if (a instanceof CacheExpire) {
                    CacheExpire ca = ((CacheExpire) a);
                    expression = ca.elExpression();
                    keyPattern = ca.keyPattern();
                }
            }
        }

        Object result = joinpoint.proceed();

        if (!StringUtils.isEmpty(expression)) {
            Expression ex = parser.parseExpression(expression);
            EvaluationContext context = new StandardEvaluationContext();
            String exResult = ex.getValue(context, result, String.class);
            keyPattern = keyPattern.replace("#{item}", exResult);
        }

        // ^id$   item.id
        chopperCacheManager.expire(keyPattern);


        return result;
    }


    public static class Test {
        String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }



    @Around(value = "IntercepterCacheable()")
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
                    final String finalKey = ca.keyPrefix() + key;
                    ChopperCacheManager cacheManager = chopperCacheManager;
                    if (!ca.cacheManager().equals(void.class)) {
                        Object m = this.applicationContext.getBean(ca.cacheManager());
                        if (m instanceof ChopperCacheManager) {
                            cacheManager = (ChopperCacheManager) m;
                        }
                    }
                    Object cacheResult = cacheManager.get(finalKey);
                    if (cacheResult != null) {
                        if (log.isDebugEnabled()) {
                            log.debug(" get result from cache , class {}, key {}", method.getDeclaringClass().getName(), finalKey);
                        }
                        return cacheResult;
                    }

                    Object rtv = joinpoint.proceed();
                    long expiredTime = ca.expireTime();
                    cacheManager.put(finalKey, rtv, expiredTime);
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
