package com.gym.coze;

/**
 * SSE流式转发封装
 */
public interface CozeSseEmitter {

    void send(String data);

    void complete();

    void error(Throwable t);

    boolean isCompleted();
}
