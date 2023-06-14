package com.arthur.authz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.arthur.rest.exceptions.DefaultAPIErrorCodes;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Spring Security will trigger this entrypoint when the {@link com.arthur.authz.StatelessAuthenticationFilter} failed to process the request due to: </br>
 * - The external token is missing in the header </br>
 * - The internal token is not found </br>
 * - The external/internal token is expired </br>
 * Return the statusCode 401 as well as the error message
 */

@Log4j2
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
      throws IOException {

    log.warn(authException.getMessage(), authException);

    String message = objectMapper.writeValueAsString(
        ErrorResponse.from(DefaultAPIErrorCodes.AUTHENTICATION_FAILED)
    );

    response.setContentType(MediaType.APPLICATION_JSON.getType());
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.getWriter().write(message);
  }
}