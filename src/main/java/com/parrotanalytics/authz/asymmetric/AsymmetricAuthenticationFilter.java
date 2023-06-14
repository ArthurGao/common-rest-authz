package com.arthur.authz.asymmetric;

import static java.lang.String.format;

import com.arthur.authz.TokenHandler;
import com.arthur.authz.asymmetric.util.JwtES256Utils;
import com.arthur.authz.models.InternalUserClaim;
import com.arthur.authz.models.UserAuthentication;
import com.arthur.authz.service.AuthUserService;
import com.arthur.rest.exceptions.ForbiddenException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Log4j2
@Component
public class AsymmetricAuthenticationFilter extends OncePerRequestFilter {

  public static final String AUTH_TOKEN = "X-AUTH-TOKEN";
  private static final String[] EXCLUDED_URLS = new String[]{"/health", "/authenticate", "/mock", "/user/password/**"};

  private final TokenHandler tokenHandler;
  private final JwtES256Utils jwtES256Utils;
  private final AuthUserService authUserService;

  @Autowired
  public AsymmetricAuthenticationFilter(JwtES256Utils jwtES256Utils, TokenHandler tokenHandler, AuthUserService authUserService) {
    this.jwtES256Utils = jwtES256Utils;
    this.tokenHandler = tokenHandler;
    this.authUserService = authUserService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    try {
      String authHeaderValue = request.getHeader(AUTH_TOKEN);
      if (StringUtils.isBlank(authHeaderValue)) {
        log.error("Request path is {}", request.getRequestURI());
        throw new ForbiddenException("Missing valid authentication header");
      }

      String userEmail = jwtES256Utils.decodeJwtToken(authHeaderValue, Map.class).get("email").toString();
      Optional<InternalUserClaim> internalUserClaim = getInternalUserClaim(userEmail);
      UserAuthentication userAuthentication = internalUserClaim
          .map(UserAuthentication::new)
          .orElseThrow(() -> new ForbiddenException(format("Found no internal token for userEmailAddress: %s", userEmail)));

      SecurityContextHolder.getContext().setAuthentication(userAuthentication);

    } catch (Exception e) {
      log.error(format("AuthenticationFilter failed, caused by: %s", e.getMessage()), e);
    }
    filterChain.doFilter(request, response);

  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String url = request.getRequestURI();
    AntPathMatcher pathMatcher = new AntPathMatcher();
    return Arrays.stream(EXCLUDED_URLS).anyMatch(x -> pathMatcher.match(x, url));
  }

  private Optional<InternalUserClaim> getInternalUserClaim(String userEmail) {
    return authUserService.getInternalUserClaimFromEmailAddress(userEmail);
  }
}
