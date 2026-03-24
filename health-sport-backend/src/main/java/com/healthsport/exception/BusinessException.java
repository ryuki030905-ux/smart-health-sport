package com.healthsport.exception;

// BusinessException：异常处理相关，统一错误返回
public class BusinessException extends RuntimeException {

    // 业务状态码，和 message 一起给前端
    private final Integer code;

    // 业务异常：用来表示可预期的业务错误
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    // 获取一下Code，给后续流程使用，这样写应该没问题
    public Integer getCode() {
        return code;
    }
}

