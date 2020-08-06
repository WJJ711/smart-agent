package com.baixiao.agent;

import java.lang.annotation.*;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/7 17:30
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Computation {
}
