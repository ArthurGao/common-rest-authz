package com.arthur.rest.exceptions.handler;

import com.arthur.rest.controller.ErrorResponseDto;
import com.arthur.rest.exceptions.CustomizedException;
import com.arthur.rest.exceptions.ErrorCode;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@Log4j2
@ControllerAdvice
public class CustomizedExceptionHandler extends RestfulAPIExceptionHandlerBase<ErrorResponseDto> {

  @ExceptionHandler(CustomizedException.class)
  private ResponseEntity<ErrorResponseDto> handleCustomizedException(
      CustomizedException ex, WebRequest request) {
    log.warn("Illegal customized exception for request {} due to {}.", request.getContextPath(),
        ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.HTTP_VERSION_NOT_SUPPORTED)
        .body(createErrorResponse(ex.getErrorCode()));
  }

  @Override
  public ErrorResponseDto createErrorResponse(ErrorCode apiErrorCode) {
    ErrorResponseDto errorResponseDto = new ErrorResponseDto();
    errorResponseDto.setCode(apiErrorCode.getErrorCode());
    errorResponseDto.setMessage(apiErrorCode.getMessage());
    return errorResponseDto;
  }
}
