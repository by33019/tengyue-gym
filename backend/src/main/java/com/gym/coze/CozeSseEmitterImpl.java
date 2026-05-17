package com.gym.coze;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
public class CozeSseEmitterImpl implements CozeSseEmitter {

    private final SseEmitter emitter;
    private volatile boolean completed = false;

    public CozeSseEmitterImpl(SseEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public void send(String data) {
        if (completed) return;
        try {
            // 使用 SseEmitter.event().data() 直接发送，不指定 event name 减少开销
            emitter.send(SseEmitter.event().data(data));
        } catch (IOException e) {
            log.warn("SSE send failed, client may have disconnected", e);
            completed = true;
        }
    }

    @Override
    public void complete() {
        if (completed) return;
        completed = true;
        try {
            emitter.send(SseEmitter.event().data("[DONE]"));
            emitter.complete();
        } catch (IOException e) {
            log.warn("SSE complete send failed", e);
        }
    }

    @Override
    public void error(Throwable t) {
        if (completed) return;
        completed = true;
        try {
            emitter.send(SseEmitter.event().name("error")
                    .data(t.getMessage() != null ? t.getMessage() : "服务异常"));
        } catch (IOException ignored) {
        }
        emitter.completeWithError(t);
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }
}
