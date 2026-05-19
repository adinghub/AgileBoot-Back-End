package com.healthtrail.common.core.dto;

import com.healthtrail.common.exception.ApiException;
import com.healthtrail.common.exception.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 响应信息主体
 *
 * @author valarchie
 */
@Data
@AllArgsConstructor
public class ResponseDTO<T> {

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    @JsonInclude
    private T data;

    public static <T> ResponseDTO<T> ok() {
        return build(null, ErrorCode.SUCCESS.code(), ErrorCode.SUCCESS.message());
    }

    public static <T> ResponseDTO<T> ok(T data) {
        return build(data, ErrorCode.SUCCESS.code(), ErrorCode.SUCCESS.message());
    }

    /**
     * 返回成功结果，并允许携带更贴近业务动作的提示文案。
     *
     * <p>异步任务场景经常不是“立刻完成”，而是“已成功受理、稍后处理”。
     * 如果仍然只返回统一的“操作成功”，App 端就必须自己硬编码说明文案。
     * 这里补一个重载，方便后端直接返回更准确的提示口径。
     */
    public static <T> ResponseDTO<T> ok(T data, String msg) {
        return build(data, ErrorCode.SUCCESS.code(), msg);
    }

    /**
     * 返回不带数据、但带自定义提示文案的成功响应。
     */
    public static <T> ResponseDTO<T> ok(String msg) {
        return build(null, ErrorCode.SUCCESS.code(), msg);
    }

    public static <T> ResponseDTO<T> fail() {
        return build(null, ErrorCode.FAILED.code(), ErrorCode.FAILED.message());
    }

    public static <T> ResponseDTO<T> fail(T data) {
        return build(data, ErrorCode.FAILED.code(), ErrorCode.FAILED.message());
    }

    public static <T> ResponseDTO<T> fail(ApiException exception) {
        return build(null, exception.getErrorCode().code(), exception.getMessage());
    }

    public static <T> ResponseDTO<T> fail(ApiException exception, T data) {
        return build(data, exception.getErrorCode().code(), exception.getMessage());
    }

    public static <T> ResponseDTO<T> build(T data, Integer code, String msg) {
        return new ResponseDTO<>(code, msg, data);
    }

    // 去掉直接填充错误码的方式， 这种方式不能拿到i18n的错误消息  统一通过ApiException来构造错误消息
//    public static <T> ResponseDTO<T> fail(ErrorCodeInterface code, Object... args) {
//        return build(null, code, args);
//    }

}

