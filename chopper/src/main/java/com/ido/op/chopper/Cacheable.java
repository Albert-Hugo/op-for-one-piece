package com.ido.op.chopper;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {

    /**
     * if key is empty , then will using key strategy to generate key
     *
     * @return
     */
    String key() default "";

    /**
     * key strategy to generate key
     *
     * @return
     */
    Class keyStrategy() default AllParameterKeyStrategy.class;

    /**
     * 0 means will not expired
     * seconds base
     *
     * @return
     */
    long expireTime() default 0;


}
