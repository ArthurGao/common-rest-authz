package com.arthur.rest.exceptions;

import lombok.Getter;

public class ServerInternalException extends RuntimeException {

  @Getter
  private final ErrorCode errorCode;

  public ServerInternalException(ErrorCode errorCode, String message) {
    this(errorCode, message, null);
  }

  public ServerInternalException(String message) {
    this(DefaultAPIErrorCodes.INTERNAL_ERROR, message, null);
  }

  public ServerInternalException(String message, Throwable cause) {
    this(DefaultAPIErrorCodes.INTERNAL_ERROR, message, cause);
  }

  public ServerInternalException(ErrorCode errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }
}
