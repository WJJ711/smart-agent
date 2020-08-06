package com.baixiao.agent;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/7 16:33
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Component
public @interface Agent {
    @AliasFor(annotation = Component.class)
    String value() default "";
}
