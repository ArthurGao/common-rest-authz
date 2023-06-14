package com.arthur.rest.exceptions;

import lombok.Data;

@Data
public class CustomizedException extends RuntimeException {

  private final ErrorCode errorCode;


  public CustomizedException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public CustomizedException(ErrorCode errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public CustomizedException(String message) {
    super(message);
    this.errorCode = DefaultAPIErrorCodes.BAD_REQUEST;
  }

  public CustomizedException(String message, Throwable cause) {
    super(message, cause);
    this.errorCode = DefaultAPIErrorCodes.BAD_REQUEST;
  }
}
