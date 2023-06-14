package com.arthur.rest.exceptions;

public interface ApiErrorCode extends DefaultAPIErrorCodes {

  ErrorCode CUSTOMIZED_BAD_REQUEST = new ErrorCode(505, "Customized bad request");

}
