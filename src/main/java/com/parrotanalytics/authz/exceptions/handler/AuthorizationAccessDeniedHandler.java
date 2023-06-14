package com.arthur.authz.exceptions.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.arthur.authz.ErrorResponse;
import com.arthur.rest.exceptions.DefaultAPIErrorCodes;
import com.arthur.rest.exceptions.ErrorCode;
import com.arthur.rest.exceptions.handler.RestfulAPIExceptionHandlerBase;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Whenever the request failed to fulfill the required permission defined in @PreAuthorize, this handler will be trigger.
 * Return the statusCode: 403 as well as the error message
 */
@Log4j2
@ControllerAdvice
public class AuthorizationAccessDeniedHandler extends RestfulAPIExceptionHandlerBase<ErrorResponse> implements AccessDeniedHandler {

  protected final ObjectMapper objectMapper;

  public AuthorizationAccessDeniedHandler(final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
      throws IOException {

    log.error(accessDeniedException.getMessage(), accessDeniedException);

    String message = objectMapper.writeValueAsString(
        ErrorResponse.from(DefaultAPIErrorCodes.NOT_AUTHORIZED)
    );

    response.setContentType(MediaType.APPLICATION_JSON.getType());
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.getWriter().write(message);
  }

  @Override
  public ErrorResponse createErrorResponse(ErrorCode apiErrorCode) {
    return ErrorResponse.builder().errorCode(apiErrorCode.getErrorCode()).message(apiErrorCode.getMessage()).build();
  }
}