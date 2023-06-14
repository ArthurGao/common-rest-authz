package com.arthur.rest.exceptions;

import lombok.Getter;

public class ForbiddenException extends RuntimeException {

  @Getter
  private final ErrorCode errorCode;

  public ForbiddenException(ErrorCode errorCode, String message) {
    this(errorCode, message, null);
  }

  public ForbiddenException(String message) {
    this(DefaultAPIErrorCodes.NOT_AUTHORIZED, message, null);
  }

  public ForbiddenException(String message, Throwable cause) {
    this(DefaultAPIErrorCodes.NOT_AUTHORIZED, message, cause);
  }

  public ForbiddenException(ErrorCode errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }
}