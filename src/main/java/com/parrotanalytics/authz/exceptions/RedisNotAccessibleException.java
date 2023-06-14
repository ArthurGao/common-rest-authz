package com.arthur.authz.exceptions;


public class RedisNotAccessibleException extends RuntimeException {

    public RedisNotAccessibleException(String message) {
        super(message);
    }
}
