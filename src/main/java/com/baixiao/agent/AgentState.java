package com.baixiao.agent;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/7 16:49
 */
public enum AgentState {
    NEW(0), RUNNING(1), BLOCKED(2), EXITED(3);
    private int state;

    AgentState(int state) {
        this.state = state;
    }
}
