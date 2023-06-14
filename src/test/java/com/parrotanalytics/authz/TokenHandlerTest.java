package com.arthur.authz;

import com.arthur.authz.exceptions.RedisNotAccessibleException;
import com.arthur.authz.helper.JwtTokenTestHelper;
import com.arthur.authz.models.ExternalUserClaim;
import com.arthur.authz.models.InternalUserClaim;
import com.arthur.authz.service.AuthUserService;
import com.arthur.rest.exceptions.ForbiddenException;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenHandlerTest {

  private static final String EXTERNAL_FAKE_KEY = "ABCefg30KVRoZS1Nb3N0LVBvd2VyDnVsLUNvbXBhbnktM2Y4Yzc5ZTEtNjkwNC00MDY0LTg2YmMtMzhlMWIzZWJmZGY8";
  private static final String INTERNAL_FAKE_KEY = "dGhpcy1pcy1mYWtlLXNlY3JldC1rZXktbG9uZy1sb25nLWxvbmctNGRiZDQ2ODAtN2U1NC00NzQ5LTk4NzAtMDczNzQ4ZjNkNTU2";

  private static final String EMAIL_ADDRESS = "test@arthur.com";

  @Mock
  private RMapCache internalJwtCacheStore;

  @Mock
  private AuthorizationProperties configuration;

  @Mock
  private RedissonClient redissonClient;

  @Mock
  private AuthUserService authUserService;

  private TokenHandler tokenHandler;

  @BeforeEach
  void setUp() {
    lenient().when(configuration.getRedisJwtCacheKeyName()).thenReturn(RandomStringUtils.randomAlphabetic(10));
    lenient().when(redissonClient.getMapCache(anyString())).thenReturn(internalJwtCacheStore);

    tokenHandler = new TokenHandler(configuration, redissonClient, authUserService);
  }

  @Test
  void getExternalUserClaim_givenTokenAsNull_thenThrowException() {
    Assertions.assertThatExceptionOfType(ForbiddenException.class)
        .isThrownBy(() -> tokenHandler.getExternalUserClaim(null))
        .withMessageContaining("Missing authentication header");
  }

  @Test
  void getExternalUserClaim_givenTokenIsExpired_thenThrowException() {
    int expiredElapsedTimeInSeconds = -10000;
    String externalToken = JwtTokenTestHelper.createExternalToken(EMAIL_ADDRESS, expiredElapsedTimeInSeconds);

    when(configuration.getExternalPrivateKeyEncoded64()).thenReturn(EXTERNAL_FAKE_KEY);

    Assertions.assertThatExceptionOfType(ForbiddenException.class)
        .isThrownBy(() -> tokenHandler.getExternalUserClaim(externalToken))
        .withMessageContaining("Given token got expired");
  }

  @Test
  void getExternalUserClaim_givenValidToken_thenReturnClaims() {
    String externalToken = JwtTokenTestHelper.createExternalToken(EMAIL_ADDRESS);

    when(configuration.getExternalPrivateKeyEncoded64()).thenReturn(EXTERNAL_FAKE_KEY);

    ExternalUserClaim actual = tokenHandler.getExternalUserClaim(externalToken);

    assertThat(actual.getUsername()).isEqualTo(EMAIL_ADDRESS);
    assertThat(actual.getExpirationTimeInSecond()).isGreaterThan(actual.getIssuedAt());
    assertThat(actual.getIssuer()).isNotNull();
  }

  @Test
  void getInternalUserClaim_givenExternalUserClaimNull_thenThrowException() {
    Assertions.assertThatIllegalArgumentException()
        .isThrownBy(() -> tokenHandler.getInternalUserClaimFromExternalUserClaim(null))
        .withMessageContaining("Given externalUserClaim should not be null");
  }

  @Test
  void getInternalUserClaim_givenEmailAddressEmpty_thenThrowException() {
    Assertions.assertThatIllegalArgumentException()
            .isThrownBy(() -> tokenHandler.getInternalUserClaimFromExternalUserClaim(JwtTokenTestHelper.createExternalUserClaim("")))
            .withMessageContaining("Given userEmailAddress should not be null or empty");
  }

  @Test
  void getInternalUserClaim_givenExternalUserClaimExpired_whenTokenNotExists_thenReturnEmpty() {
    assertThat(tokenHandler.getInternalUserClaimFromExternalUserClaim(JwtTokenTestHelper.createExternalUserClaim(EMAIL_ADDRESS, -1)))
            .isEmpty();
  }

  @Test
  void getInternalUserClaim_givenValidInput_whenCacheIsNotFound_thenThrowException() {
    when(redissonClient.getMapCache(any())).thenReturn(null);
    assertThat(tokenHandler.getInternalUserClaimFromExternalUserClaim(JwtTokenTestHelper.createExternalUserClaim(EMAIL_ADDRESS))).isEmpty();
  }

  @Test
  void getInternalUserClaim_givenValidInput_whenCacheIsEmptyAndInternalTokenCannotBeGenerated_thenThrowException() {

    lenient().when(internalJwtCacheStore.isEmpty()).thenReturn(true);
    when(authUserService.getInternalUserClaimFromEmailAddress(EMAIL_ADDRESS)).thenReturn(Optional.empty());

    assertThat(tokenHandler.getInternalUserClaimFromExternalUserClaim(JwtTokenTestHelper.createExternalUserClaim(EMAIL_ADDRESS))).isEmpty();
  }

  @Test
  void getInternalUserClaim_givenValidInput_whenCacheIsEmptyAndInternalTokenCanBeGenerated_thenReturnValidToken() {

    InternalUserClaim internalUserClaim = JwtTokenTestHelper.createInternalUserClaim(EMAIL_ADDRESS, RandomUtils.nextInt(), RandomUtils.nextInt(), Set.of(RandomStringUtils.randomAlphabetic(10)));
    lenient().when(internalJwtCacheStore.isEmpty()).thenReturn(true);
    when(authUserService.getInternalUserClaimFromEmailAddress(EMAIL_ADDRESS)).thenReturn(Optional.of(internalUserClaim));
    when(configuration.getInternalPrivateKeyEncoded64()).thenReturn(INTERNAL_FAKE_KEY);

    assertThat(tokenHandler.getInternalUserClaimFromExternalUserClaim(JwtTokenTestHelper.createExternalUserClaim(EMAIL_ADDRESS))).isNotEmpty()
            .isEqualTo(Optional.of(internalUserClaim));
  }


  @Test
  void getInternalUserClaim_givenValidInput_whenTokenExpired_thenThrowException() {
    int userId = 1;
    int accountId = 100;
    Set<String> permissions = Set.of(RandomStringUtils.randomAlphabetic(10));
    int expiredElapsedTimeInSeconds = -10000;

    String internalToken = JwtTokenTestHelper.createInternalJwt(EMAIL_ADDRESS, userId, accountId, permissions, expiredElapsedTimeInSeconds);

    when(internalJwtCacheStore.get(EMAIL_ADDRESS)).thenReturn(internalToken);
    when(configuration.getInternalPrivateKeyEncoded64()).thenReturn(INTERNAL_FAKE_KEY);

    ExternalUserClaim externalUserClaim = JwtTokenTestHelper.createExternalUserClaim(EMAIL_ADDRESS);

    assertThatExceptionOfType(ForbiddenException.class)
        .isThrownBy(() -> tokenHandler.getInternalUserClaimFromExternalUserClaim(externalUserClaim))
        .withMessageContaining(String.format("Internal token of user email address: %s got expired", EMAIL_ADDRESS));
  }

  @Test
  void getInternalUserClaim_givenValidInput_whenTokenExists_thenReturnClaims() {
    int userId = 1;
    int accountId = 100;
    Set<String> permissions = Set.of(RandomStringUtils.randomAlphabetic(10));

    String internalToken = JwtTokenTestHelper.createInternalJwt(EMAIL_ADDRESS, userId, accountId, permissions);

    when(internalJwtCacheStore.get(EMAIL_ADDRESS)).thenReturn(internalToken);
    when(configuration.getInternalPrivateKeyEncoded64()).thenReturn(INTERNAL_FAKE_KEY);

    Optional<InternalUserClaim> actualOptional = tokenHandler.getInternalUserClaimFromExternalUserClaim(JwtTokenTestHelper.createExternalUserClaim(EMAIL_ADDRESS));

    assertThat(actualOptional).isNotEmpty();
    InternalUserClaim actual = actualOptional.get();

    assertThat(actual.getUserId()).isEqualTo(userId);
    assertThat(actual.getAccountId()).isEqualTo(accountId);
    assertThat(actual.getUsername()).isEqualTo(EMAIL_ADDRESS);
    assertThat(actual.getPermissions()).containsAll(permissions);
  }

  @Test
  void getInternalUserClaim_givenValidInput_whenRedissonClientIsNull_ThrowsRedisNotAccessibleException() {
    tokenHandler = new TokenHandler(configuration, null, authUserService);
    ExternalUserClaim externalUserClaim = JwtTokenTestHelper.createExternalUserClaim(EMAIL_ADDRESS);
    assertThatThrownBy(() -> tokenHandler.getInternalUserClaimFromExternalUserClaim(externalUserClaim))
            .isInstanceOf(RedisNotAccessibleException.class)
            .hasMessage("Redis not accessible");
  }
}