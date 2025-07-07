package com.calculator.api.model;

import java.time.LocalDateTime;

/**
 * 统一的API响应包装类
 * 提供标准化的响应格式，包含状态码、消息、数据、错误代码和时间戳
 *
 * @param <T> 响应数据的类型
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private String status;  // 业务状态码
    private T data;
    private String requestId;  // 请求追踪ID
    private LocalDateTime timestamp;

    /**
     * 默认构造函数
     */
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 构造成功响应
     *
     * @param data 响应数据
     */
    public ApiResponse(T data) {
        this.success = true;
        this.status = "SUCCESS";
        this.message = "Success";
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 构造响应
     *
     * @param success 是否成功
     * @param status  业务状态码
     * @param message 响应消息
     * @param data    响应数据
     */
    public ApiResponse(boolean success, String status, String message, T data) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 构造响应（带请求ID）
     *
     * @param success   是否成功
     * @param status    业务状态码
     * @param message   响应消息
     * @param data      响应数据
     * @param requestId 请求追踪ID
     */
    public ApiResponse(boolean success, String status, String message, T data, String requestId) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.data = data;
        this.requestId = requestId;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 创建成功响应
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }

    /**
     * 创建成功响应（带自定义消息）
     *
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "SUCCESS", message, data);
    }

    /**
     * 创建成功响应（带请求ID）
     * 注意：requestId主要通过X-Request-ID响应头传递，body中包含是为了方便前端访问
     *
     * @param message   响应消息
     * @param data      响应数据
     * @param requestId 请求追踪ID
     * @param <T>       数据类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> success(String message, T data, String requestId) {
        return new ApiResponse<>(true, "SUCCESS", message, data, requestId);
    }

    /**
     * 创建失败响应
     *
     * @param status  状态码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> error(String status, String message) {
        return new ApiResponse<>(false, status, message, null);
    }

    /**
     * 创建失败响应（带数据）
     *
     * @param status  状态码
     * @param message 错误消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> error(String status, String message, T data) {
        return new ApiResponse<>(false, status, message, data);
    }

    /**
     * 创建失败响应（带请求ID）
     *
     * @param status    状态码
     * @param message   错误消息
     * @param requestId 请求追踪ID
     * @param <T>       数据类型
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> error(String status, String message, String requestId) {
        return new ApiResponse<>(false, status, message, null, requestId);
    }

    // Getters and Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", data=" + data +
                ", requestId='" + requestId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
} 