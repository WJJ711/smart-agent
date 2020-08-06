package com.baixiao.agent;



/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/7 16:53
 */
@FunctionalInterface
public interface AsyncHandler<T> {
    void onCompleted(AsyncResult<T> asyncResult);
}
