package com.arthur.rest.exceptions.handler;

import com.arthur.rest.exceptions.BadRequestException;
import com.arthur.rest.exceptions.DefaultAPIErrorCodes;
import com.arthur.rest.exceptions.ErrorCode;
import com.arthur.rest.exceptions.ForbiddenException;
import com.arthur.rest.exceptions.ServerInternalException;
import javax.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Log4j2
@ControllerAdvice
public abstract class RestfulAPIExceptionHandlerBase<T> extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
      HttpHeaders headers, HttpStatus status, WebRequest request) {
    log.warn("Failed to handle request {} [status: {}] due to {}.", request.getContextPath(),
        status, ex.getMessage(), ex);
    if (ex instanceof NoHandlerFoundException) {
      return ResponseEntity.notFound()
          .build();
    }
    return ResponseEntity.internalServerError()
        .body(createErrorResponse(DefaultAPIErrorCodes.INTERNAL_ERROR));
  }

  @ExceptionHandler({MethodArgumentTypeMismatchException.class, ConstraintViolationException.class, IllegalArgumentException.class})
  private ResponseEntity<T> handleInvalidArgument(
      RuntimeException ex, WebRequest request) {
    log.warn("Bad request for request {} due to {}.", request.getContextPath(),
        ex.getMessage(), ex);
    return ResponseEntity.badRequest()
        .body(createErrorResponse(DefaultAPIErrorCodes.BAD_REQUEST));
  }

  @ExceptionHandler(BadRequestException.class)
  private ResponseEntity<T> handleBadRequestRuntimeException(
      BadRequestException ex, WebRequest request) {
    log.warn("Bad request exception for request {} due to {}.", request.getContextPath(),
        ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(createErrorResponse(ex.getErrorCode()));
  }

  @ExceptionHandler(ForbiddenException.class)
  private ResponseEntity<T> handleForbiddenRuntimeException(
      ForbiddenException ex, WebRequest request) {
    log.warn("Forbidden exception for request {} due to {}.", request.getContextPath(),
        ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(createErrorResponse(ex.getErrorCode()));
  }

  @ExceptionHandler(ServerInternalException.class)
  public ResponseEntity<T> handleServerInternalException(ServerInternalException ex,
      WebRequest request) {
    log.warn("Internal error for request {} due to {}.", request.getContextPath(),
        ex.getMessage(), ex);
    return ResponseEntity.internalServerError()
        .body(createErrorResponse(ex.getErrorCode()));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<T> handleRuntimeException(RuntimeException ex,
      WebRequest request) {
    log.warn("Internal error for request {} due to {}.", request.getContextPath(),
        ex.getMessage(), ex);
    return ResponseEntity.internalServerError()
        .body(createErrorResponse(DefaultAPIErrorCodes.INTERNAL_ERROR));
  }

  public abstract T createErrorResponse(ErrorCode apiErrorCode);
}
