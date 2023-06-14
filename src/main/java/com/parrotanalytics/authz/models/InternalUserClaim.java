package com.arthur.authz.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jsonwebtoken.Claims;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Class contains user information claimed from the internal token
 */
@Value
@Builder
@Jacksonized
public class InternalUserClaim {

  @JsonProperty(value = Claims.EXPIRATION)
  private long expirationTimeInSecond;

  @JsonProperty(value = Claims.ISSUED_AT)
  private long issuedAt;

  @JsonProperty(value = Claims.ISSUER)
  private String issuer;

  @JsonProperty(value = Claims.ID)
  private String tokenUUID;

  @JsonProperty(value = "pun")
  private String username;

  @JsonProperty(value = "user_id")
  private int userId;

  @JsonProperty(value = "account_id")
  private int accountId;

  @JsonProperty(value = "permissions")
  private Set<String> permissions;

  public boolean isTokenNotExpired() {
    return secondsToExpire() >= 0;
  }

  public long secondsToExpire() {
    return this.expirationTimeInSecond - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
  }
}
