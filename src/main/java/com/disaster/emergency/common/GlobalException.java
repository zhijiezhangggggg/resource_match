package com.disaster.emergency.common;

import lombok.Data;

@Data
public class GlobalException extends RuntimeException {
    private Integer code;
    private String message;

    public GlobalException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public GlobalException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }
}
