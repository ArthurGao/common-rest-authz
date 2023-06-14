package com.arthur.authz;

import com.arthur.authz.exceptions.RedisNotAccessibleException;
import com.arthur.authz.models.ExternalUserClaim;
import com.arthur.authz.models.InternalUserClaim;
import com.arthur.authz.models.UserAuthentication;
import com.arthur.authz.service.AuthUserService;
import com.arthur.authz.util.MaskUtil;
import com.arthur.rest.exceptions.ForbiddenException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static java.lang.String.format;

@Log4j2
@Component
public class StatelessAuthenticationFilter extends OncePerRequestFilter {

  public static final String AUTH_TOKEN = "X-AUTH-TOKEN";
  public static final String EXTERNAL_API_KEY = "X-API-KEY";
  public static final String AUTHORISATION = "Authorisation";
  private static final String[] EXCLUDED_URLS = new String[]{"/health", "/authenticate", "/mock", "/user/password/**"};

  private final TokenHandler tokenHandler;
  private final AuthUserService authUserService;

  public StatelessAuthenticationFilter(TokenHandler tokenHandler, AuthUserService authUserService) {
    this.tokenHandler = tokenHandler;
    this.authUserService = authUserService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    try {
      String authHeaderValue = request.getHeader(AUTH_TOKEN);
      String authApiKey = request.getHeader(EXTERNAL_API_KEY);
      String authorisation = request.getHeader(AUTHORISATION);

      if (StringUtils.isBlank(authHeaderValue) && StringUtils.isBlank(authApiKey) && StringUtils.isBlank(authorisation)) {
        log.error("Request path is {}", request.getRequestURI());
        throw new ForbiddenException("Missing valid authentication header");
      }

      UserAuthentication userAuthentication;

      if (StringUtils.isNotBlank(authHeaderValue)) {

        ExternalUserClaim externalUserClaim = tokenHandler.getExternalUserClaim(authHeaderValue);

        Optional<InternalUserClaim> internalUserClaim = getInternalUserClaim(externalUserClaim);
        userAuthentication = internalUserClaim
                .map(UserAuthentication::new)
                .orElseThrow(() -> new ForbiddenException(format("Found no internal token for userEmailAddress: %s", externalUserClaim.getUsername())));
      } else if (StringUtils.isNotBlank(authApiKey)) {
        Optional<InternalUserClaim> internalUserClaim = tokenHandler.getUserInternalClaimForApiKey(authApiKey);
        userAuthentication = internalUserClaim
                .map(UserAuthentication::new)
                .orElseThrow(() -> new ForbiddenException(format("Found no internal token for API KEY: %s", MaskUtil.mask(authApiKey))));
      } else {
        Optional<InternalUserClaim> internalUserClaim = tokenHandler.getUserInternalClaimForAuthorization(authorisation);
        userAuthentication = internalUserClaim
                .map(UserAuthentication::new)
                .orElseThrow(() -> new ForbiddenException(format("Found no internal token for authorisation: %s", authorisation)));
      }

      SecurityContextHolder.getContext().setAuthentication(userAuthentication);

    } catch (Exception e) {
      log.error(String.format("AuthenticationFilter failed, caused by: %s", e.getMessage()), e);
    }
    filterChain.doFilter(request, response);

  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String url = request.getRequestURI();
    AntPathMatcher pathMatcher = new AntPathMatcher();
    return Arrays.stream(EXCLUDED_URLS).anyMatch(x -> pathMatcher.match(x, url));
  }

  private Optional<InternalUserClaim> getInternalUserClaim(ExternalUserClaim externalUserClaim) {
    Optional<InternalUserClaim> internalUserClaim;
    try {
      internalUserClaim = tokenHandler.getInternalUserClaimFromExternalUserClaim(externalUserClaim);
    } catch (RedisNotAccessibleException e) {
      log.warn("Redis was not accessible, Try to get internal claim from RDS, user Id is {}", externalUserClaim.getUsername(), e);
      internalUserClaim = authUserService.getInternalUserClaimFromEmailAddress(externalUserClaim.getUsername());
    }
    return internalUserClaim;
  }
}
