package com.arthur.authz;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.arthur.authz.exceptions.RedisNotAccessibleException;
import com.arthur.authz.models.ExternalAuthClaim;
import com.arthur.authz.models.ExternalUserClaim;
import com.arthur.authz.models.InternalUserClaim;
import com.arthur.authz.service.AuthUserService;
import com.arthur.authz.util.MaskUtil;
import com.arthur.rest.exceptions.ForbiddenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

@Log4j2
@Component
public class TokenHandler {

  private static final String BEARER = "bearer";
  private final ObjectMapper objectMapper;

  private final AuthorizationProperties properties;

  private final RedissonClient redissonClient;

  private final AuthUserService authUserService;

  @Autowired
  public TokenHandler(@NonNull final AuthorizationProperties properties, @Nullable RedissonClient redissonClient, AuthUserService authUserService) {
    this.authUserService = authUserService;
    this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    this.properties = properties;
    this.redissonClient = redissonClient;
  }

  /**
   * Parse given token into  <{@link ExternalUserClaim}. Throw runtime exception ForbiddenRuntimeException when the token is expired.
   *
   * @return <{@link ExternalUserClaim}>
   * @throws {@link io.jsonwebtoken.ExpiredJwtException} when the token is expired
   */
  public ExternalUserClaim getExternalUserClaim(String externalToken) {

    if (externalToken == null || externalToken.isEmpty()) {
      throw new ForbiddenException("Missing authentication header");
    }

    try {
      return objectMapper.convertValue(getClaimsBody(properties.getExternalPrivateKeyEncoded64(), externalToken), ExternalUserClaim.class);
    } catch (ExpiredJwtException e) {
      throw new ForbiddenException("Given token got expired", e);
    }
  }

  public ExternalAuthClaim getExternalAuthClaim(String externalToken) {
    try {
      return objectMapper.convertValue(getClaimsBody(properties.getExternalPrivateKeyEncoded64(), externalToken), ExternalAuthClaim.class);
    } catch (ExpiredJwtException e) {
      throw new ForbiddenException("Given token got expired", e);
    }
  }

  private Claims getClaimsBody(String properties, String externalToken) {
    return Jwts.parserBuilder()
          .setSigningKey(properties)
          .build()
          .parseClaimsJws(externalToken)
          .getBody();
  }

  /**
   * Return an {@link Optional}<{@link InternalUserClaim}> if there is a cache entry associated with given userEmailAddress, otherwise generate a token from DB and save it in cache if possible, otherwise return empty
   *
   * @param externalUserClaim the object for claim
   * @return {@link Optional}<{@link InternalUserClaim}>
   */
  public Optional<InternalUserClaim> getInternalUserClaimFromExternalUserClaim(ExternalUserClaim externalUserClaim) {

    if(externalUserClaim == null) {
      throw new IllegalArgumentException("Given externalUserClaim should not be null");
    }

    String emailAddress = externalUserClaim.getUsername();

    if (StringUtils.isBlank(emailAddress)) {
      throw new IllegalArgumentException("Given userEmailAddress should not be null or empty");
    }



    if (redissonClient == null) {
      throw new RedisNotAccessibleException("Redis not accessible");
    }

    RMapCache<String, String> mapCache = redissonClient.getMapCache(properties.getRedisJwtCacheKeyName());

    if (mapCache == null) {
      log.warn("There's no cache with the name {}, got error while trying to get token for user {}", properties.getRedisJwtCacheKeyName(), emailAddress);
      return Optional.empty();
    }

    String internalToken = mapCache.get(emailAddress);

    if (!externalUserClaim.isTokenNotExpired()) {
      log.warn("External token was expired for user {}", emailAddress);
      return Optional.empty();
    } else if (internalToken == null || internalToken.isEmpty()) {
      return saveInternalUserClaimAndReturn(getInternalUserClaimFromDb(emailAddress), emailAddress, externalUserClaim.secondsToExpire());
    } else {
      log.debug("Internal user claim is in the cache for user {}", emailAddress);
      return getInternalUserClaim(internalToken, emailAddress);
    }

  }

  private Optional<InternalUserClaim> getInternalUserClaimFromDb(String emailAddress) {
    log.warn("There is no internal token with the user name {}, hence creating a new one", emailAddress);
    Optional<InternalUserClaim> internalUserClaim = authUserService.getInternalUserClaimFromEmailAddress(emailAddress);
    if (internalUserClaim.isEmpty()) {
      log.warn("Cannot create an internal token for the user {}", emailAddress);
      return Optional.empty();
    }
    return internalUserClaim;
  }


  public void saveInternalToken(InternalUserClaim internalUserClaim, Long secondsToExpire) {
    log.debug("Internal token wasn't found in the redis, but redis is acccessible, so saving internal token for user {}", internalUserClaim.getUsername());

    String jwtToken = Jwts.builder()
            .setClaims(objectMapper.convertValue(internalUserClaim, Map.class))
            .signWith(SignatureAlgorithm.HS512, properties.getInternalPrivateKeyEncoded64())
            .compact();

    redissonClient.getMapCache(properties.getRedisJwtCacheKeyName()).putIfAbsent(internalUserClaim.getUsername(), jwtToken, secondsToExpire, TimeUnit.SECONDS);
  }

  public Optional<InternalUserClaim> getUserInternalClaimForApiKey(String authApiKey) {
    log.debug("Auth API Key is {}", MaskUtil.mask(authApiKey));
    return authUserService.getInternalUserClaimFromApiKey(authApiKey);
  }

  public Optional<InternalUserClaim> getUserInternalClaimForAuthorization(String authorisation) {
    if (StringUtils.startsWithIgnoreCase(authorisation, BEARER)) {
      String jwtToken = authorisation.substring(BEARER.length());
      ExternalAuthClaim externalAuthClaim = getExternalAuthClaim(jwtToken);

      if (externalAuthClaim == null || !externalAuthClaim.isTokenNotExpired()) {
        log.warn("External auth claim is null or expired");
        return Optional.empty();
      }

      if (redissonClient != null) {
        RMapCache<String, String> mapCache = redissonClient.getMapCache(properties.getRedisJwtCacheKeyName());
        if (mapCache.containsKey(externalAuthClaim.getEmail())) {
          String internalClaimToken = mapCache.get(externalAuthClaim.getEmail());
          log.debug("Internal user claim is in the cache for user {}", externalAuthClaim.getEmail());
          return getInternalUserClaim(internalClaimToken, externalAuthClaim.getEmail());
        } else {
          saveInternalUserClaimAndReturn(getInternalUserClaimFromDb(externalAuthClaim.getEmail()), externalAuthClaim.getEmail(), externalAuthClaim.secondsToExpire());
        }
      }
      return getInternalUserClaimFromDb(externalAuthClaim.getEmail());
    } else {
      log.warn("Authorization does not begin with bearer string in request {}", authorisation);
      return Optional.empty();
    }
  }

  private Optional<InternalUserClaim> saveInternalUserClaimAndReturn(Optional<InternalUserClaim> internalUserClaim, String emailAddress, long secondsToExpire) {
    if (internalUserClaim.isEmpty()) {
      log.warn("Unable to create internal user claim for user {}", emailAddress);
      return Optional.empty();
    }

    saveInternalToken(internalUserClaim.get(), secondsToExpire);
    return internalUserClaim;
  }

  private Optional<InternalUserClaim> getInternalUserClaim(String internalClaimToken, String emailAddress) {
    try {
      InternalUserClaim internalUserClaim = this.objectMapper.convertValue(
              getClaimsBody(properties.getInternalPrivateKeyEncoded64(), internalClaimToken),
              InternalUserClaim.class
      );
      return Optional.of(internalUserClaim);
    } catch (ExpiredJwtException e) {
      throw new ForbiddenException(format("Internal token of user email address: %s got expired", emailAddress), e);
    }
  }
}
