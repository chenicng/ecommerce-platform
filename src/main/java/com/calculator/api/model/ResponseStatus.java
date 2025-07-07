package com.calculator.api.model;

/**
 * 业务响应状态枚举
 * 定义标准化的状态码，便于前端进行精确的业务逻辑处理
 */
public enum ResponseStatus {
    
    // 成功
    SUCCESS("SUCCESS", "操作成功"),
    
    // 客户端错误 (4xx)
    INVALID_REQUEST("INVALID_REQUEST", "无效请求"),
    VALIDATION_ERROR("VALIDATION_ERROR", "参数验证失败"),
    INVALID_JSON_FORMAT("INVALID_JSON_FORMAT", "JSON格式错误"),
    INVALID_NUMBER_FORMAT("INVALID_NUMBER_FORMAT", "数值格式错误"),
    
    // 计算相关错误
    DIVISION_BY_ZERO("DIVISION_BY_ZERO", "除数不能为零"),
    ARITHMETIC_ERROR("ARITHMETIC_ERROR", "算术运算错误"),
    PRECISION_OVERFLOW("PRECISION_OVERFLOW", "精度溢出"),
    
    // 服务器错误 (5xx)
    INTERNAL_ERROR("INTERNAL_ERROR", "内部服务器错误"),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "服务不可用");

    private final String code;
    private final String description;

    ResponseStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code;
    }
} 