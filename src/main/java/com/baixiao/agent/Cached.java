package com.baixiao.agent;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/7 17:13
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Cached {
    int corePoolSize() default 0;
    int maximumPoolSize() default Integer.MAX_VALUE;
    long keepAliveTime() default 60L;
    TimeUnit unit() default TimeUnit.SECONDS;
}
