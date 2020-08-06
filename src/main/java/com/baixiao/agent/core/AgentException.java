package com.baixiao.agent.core;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/7 21:25
 */
public class AgentException extends RuntimeException {
    private static int startCode=1000;

    public static final int FAILED_TO_LOCK_AGENT_MESSAGE_QUEUE=startCode++;
    public static final String FAILED_TO_LOCK_AGENT_MESSAGE_QUEUE_MESSAGE="Failed to lock the message queue of agent {0}.";

    public static final int FAILED_TO_WAIT_FOR_SYNC_INVOKING=startCode++;
    public static final String FAILED_TO_WAIT_FOR_SYNC_INVOKING_MESSAGE="Failed to wait for sync invoking of agent {0}.";

    public static final int ASYNC_WITH_EXCEPTION=startCode++;
    public static final String ASYNC_WITH_EXCEPTION_MESSAGE="Failed to invoke the async method.";

    public static final int ASYNC_METHOD_RESULT_TYPE_ERROR=startCode++;
    public static final String ASYNC_METHOD_RESULT_TYPE_ERROR_MESSAGE="The result type of async method of agent {0} is not AgentCallback<?>.";

    public static final int AGENT_NOT_STARTED=startCode++;
    public static final String AGENT_NOT_STARTED_MESSAGE="The agent {0} is not started.";

    public static final int AGENT_NOT_NEW=startCode++;
    public static final String AGENT_NOT_NEW_MESSAGE="The agent {0} is not in new state.";

    private final int statusCode;

    public static int getStartCode() {
        return startCode;
    }
    public AgentException(int statusCode,String message){
        super(message);
        this.statusCode=statusCode;
    }
    public AgentException(int statusCode,String message,Throwable cause){
        super(message,cause);
        this.statusCode=statusCode;
    }
    public AgentException(int statusCode,Throwable cause){
        super(cause);
        this.statusCode=statusCode;
    }
    protected AgentException(int statusCode,String message,Throwable cause,
                             boolean enableSuppression,
                             boolean writableStackTrace){
        super(message,cause,enableSuppression,writableStackTrace);
        this.statusCode=statusCode;
    }
}
