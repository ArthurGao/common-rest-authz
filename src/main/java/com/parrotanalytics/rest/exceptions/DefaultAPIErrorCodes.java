package com.arthur.rest.exceptions;

public interface DefaultAPIErrorCodes {

  ErrorCode BAD_REQUEST = new ErrorCode(40000, "One or more of the arguments are invalid.");
  ErrorCode INTERNAL_ERROR = new ErrorCode(50000, "Could not process the request. Please try again later.");
  ErrorCode AUTHENTICATION_FAILED = new ErrorCode(40100, "Authentication failed");
  ErrorCode RESOURCE_NOT_FOUND = new ErrorCode(40400, "Resource not found");
  ErrorCode NOT_AUTHORIZED = new ErrorCode(40300, "Not authorized to perform the operation");
  ErrorCode FORBIDDEN_NO_PERMISSION = new ErrorCode(40301,
      "Not authorized to access the requested resource(s)");
  ErrorCode FORBIDDEN_TOKEN_EXPIRED = new ErrorCode(40302,
      "Not authorized to access the requested resource(s) because token expiry.");

}