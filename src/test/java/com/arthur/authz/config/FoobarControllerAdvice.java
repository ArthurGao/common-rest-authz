package com.arthur.authz.config;

import com.arthur.authz.ErrorResponse;
import com.arthur.rest.exceptions.DefaultAPIErrorCodes;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Log4j2
public class FoobarControllerAdvice extends ResponseEntityExceptionHandler {

  @ExceptionHandler(AccessDeniedException.class)
  private ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {

    log.warn("Access denied for request {} due to {}.", request.getContextPath(), ex.getMessage(), ex);

    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ErrorResponse.from(DefaultAPIErrorCodes.NOT_AUTHORIZED));
  }
}
