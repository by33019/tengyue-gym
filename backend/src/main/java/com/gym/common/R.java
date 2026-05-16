package com.gym.common;

import lombok.Data;

@Data
public class R<T> {
    private int code;
    private String message;
    private T data;

    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> R<T> ok(T data) {
        return new R<>(200, "success", data);
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }

    public static <T> R<T> fail(String message) {
        return fail(400, message);
    }

    public static <T> R<T> unauthorized() {
        return fail(401, "未登录或登录已过期");
    }

    public static <T> R<T> forbidden() {
        return fail(403, "无权限");
    }

    public static <T> R<T> serverError() {
        return fail(500, "服务器异常");
    }
}
