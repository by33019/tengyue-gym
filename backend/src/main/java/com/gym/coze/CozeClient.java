package com.gym.coze;

/**
 * Coze API HTTP客户端，处理SSE流式请求
 */
public interface CozeClient {

    /**
     * 发送请求到Coze工作流并返回SSE流
     */
    void streamRun(String prompt, String sessionId, CozeSseEmitter emitter);
}
