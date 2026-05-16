package com.gym.coze;

/**
 * 用户上下文组装，将身体数据与运动摘要拼接为prompt前缀
 */
public interface ContextBuilder {

    /**
     * 为指定用户构建AI请求的上下文文本
     */
    String buildContext(Long userId);
}
