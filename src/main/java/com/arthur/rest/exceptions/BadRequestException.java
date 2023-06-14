package com.arthur.rest.exceptions;

import lombok.Getter;

public class BadRequestException extends RuntimeException {

  @Getter
  private final ErrorCode errorCode;

  public BadRequestException(ErrorCode errorCode, String message) {
    this(errorCode, message, null);
  }

  public BadRequestException(String message) {
    this(DefaultAPIErrorCodes.BAD_REQUEST, message, null);
  }

  public BadRequestException(String message, Throwable cause) {
    this(DefaultAPIErrorCodes.BAD_REQUEST, message, cause);
  }

  public BadRequestException(ErrorCode errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }
}
