package com.ido.op.chopper;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Ido
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheExpire {

    /**
     * 使符合 正则表达式的 key 过期
     * 1.例如： 包含 abc 的key 过期-》   .*abc.*
     * <p>
     * 2.结合 exExpression 的返回值一起使用
     * .*#{item}.* ， {@link CacheAspect} 会将 exExpression 的返回值 替换掉 #{item} 形成新的正则表达式
     *
     * @return
     */
    String keyPattern();

    /**
     * 从被注解的方法返回结果中，解析EL 表达式，获取的结果作为key pattern 使符合 key pattern 的缓存过期
     *
     * @return
     */
    String elExpression() default "";


}
