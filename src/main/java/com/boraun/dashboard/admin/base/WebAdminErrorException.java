package com.boraun.dashboard.admin.base;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class WebAdminErrorException extends RuntimeException {
    private final String message;
    private final HttpStatus httpStatus;

    public WebAdminErrorException(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
