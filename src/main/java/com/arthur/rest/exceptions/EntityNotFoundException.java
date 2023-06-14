package com.arthur.rest.exceptions;

import lombok.Getter;

public class EntityNotFoundException extends RuntimeException {

  @Getter
  private final ErrorCode errorCode;

  public EntityNotFoundException(ErrorCode errorCode, String message) {
    this(errorCode, message, null);
  }

  public EntityNotFoundException(String message) {
    this(DefaultAPIErrorCodes.RESOURCE_NOT_FOUND, message, null);
  }

  public EntityNotFoundException(String message, Throwable cause) {
    this(DefaultAPIErrorCodes.RESOURCE_NOT_FOUND, message, cause);
  }

  public EntityNotFoundException(ErrorCode errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }
}
