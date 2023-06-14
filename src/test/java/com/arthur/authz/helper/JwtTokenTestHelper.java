package com.arthur.authz.helper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.arthur.authz.models.ExternalAuthClaim;
import com.arthur.authz.models.ExternalUserClaim;
import com.arthur.authz.models.InternalUserClaim;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.RandomUtils;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JwtTokenTestHelper {

  private static final ObjectMapper mapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private static final int expiryElapsedTimeInSeconds = 300000600;

  private static final String ISSUER = "arthur.com";

  private final static String ENCODED64_SESSION_SECRET = "ABCefg30KVRoZS1Nb3N0LVBvd2VyDnVsLUNvbXBhbnktM2Y4Yzc5ZTEtNjkwNC00MDY0LTg2YmMtMzhlMWIzZWJmZGY8"; //This is a fake secret

  private static final String INTERNAL_PRIVATE_KEY_ENCODED64 = "dGhpcy1pcy1mYWtlLXNlY3JldC1rZXktbG9uZy1sb25nLWxvbmctNGRiZDQ2ODAtN2U1NC00NzQ5LTk4NzAtMDczNzQ4ZjNkNTU2";

  public static String createExternalToken(String userName) {
    return createExternalToken(userName, expiryElapsedTimeInSeconds);
  }

  public static ExternalUserClaim createExternalUserClaim(String userName) {
    return createExternalUserClaim(userName, expiryElapsedTimeInSeconds);
  }

  public static String createExternalToken(String userName, long tokenExpiryInSeconds) {

    ExternalUserClaim externalUserClaim = createExternalUserClaim(userName, tokenExpiryInSeconds);

    Map<String, Object> claimsMap = mapper.convertValue(externalUserClaim, Map.class);

    return Jwts.builder().setClaims(claimsMap)
        .signWith(SignatureAlgorithm.HS512, ENCODED64_SESSION_SECRET)
        .compact();
  }

  public static String createExternalAuthorisation(String userName, long expirationTime) {

    ExternalAuthClaim externalAuthClaim = new ExternalAuthClaim(
            RandomUtils.nextLong(),
            RandomUtils.nextLong(),
            "test",
            userName,
            List.of(ExternalAuthClaim.Scope.builder().scopes("TVDemandDataAPI TalentDataAPI").build()),
            expirationTime,
            Instant.now().toEpochMilli(),
            ISSUER,
            UUID.randomUUID().toString()
    );

    Map<String, Object> claimsMap = mapper.convertValue(externalAuthClaim, Map.class);

    return Jwts.builder().setClaims(claimsMap)
            .signWith(SignatureAlgorithm.HS512, ENCODED64_SESSION_SECRET)
            .compact();
  }

  public static ExternalUserClaim createExternalUserClaim(String userName, long tokenExpiryInSeconds) {

    return new ExternalUserClaim(
            TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + tokenExpiryInSeconds,
            TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()),
            ISSUER,
            UUID.randomUUID().toString(),
            List.of(),
            userName
    );
  }

  public static String createInternalJwt(String userName, Integer userId, Integer accountId, Set<String> permission) {
    return createInternalJwt(userName, userId, accountId, permission, expiryElapsedTimeInSeconds);
  }

  public static String createInternalJwt(String userName, Integer userId, Integer accountId, Set<String> permission, int expiredElapsedTimeInSeconds) {
    long currentTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

    InternalUserClaim coarseGrainedClaims = createInternalUserClaim(userName, userId, accountId, permission, expiredElapsedTimeInSeconds, currentTimeInSeconds);

    return Jwts.builder()
        .setClaims(mapper.convertValue(coarseGrainedClaims, Map.class))
        .signWith(SignatureAlgorithm.HS512, INTERNAL_PRIVATE_KEY_ENCODED64)
        .compact();
  }

  public static InternalUserClaim createInternalUserClaim(String userName, Integer userId, Integer accountId, Set<String> permission) {
    return createInternalUserClaim(userName, userId, accountId, permission, expiryElapsedTimeInSeconds, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
  }

  public static InternalUserClaim createInternalUserClaim(String userName, Integer userId, Integer accountId, Set<String> permission, int expiredElapsedTimeInSeconds, long currentTimeInSeconds) {
    return InternalUserClaim.builder()
        .username(userName)
        .userId(userId)
        .accountId(accountId)
        .tokenUUID(UUID.randomUUID().toString())
        .issuedAt(currentTimeInSeconds)
        .expirationTimeInSecond(currentTimeInSeconds + expiredElapsedTimeInSeconds)
        .issuer(RandomStringUtils.randomAlphabetic(10))
        .permissions(permission)
        .build();
  }
}
