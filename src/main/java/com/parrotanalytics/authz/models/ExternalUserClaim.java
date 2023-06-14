package com.arthur.authz.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.jsonwebtoken.Claims;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.springframework.security.core.GrantedAuthority;


/**
 * Class contains user information claimed from the external token
 */
@Getter
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class ExternalUserClaim {

  @JsonProperty(value = Claims.EXPIRATION)
  private long expirationTimeInSecond;

  @JsonProperty(value = Claims.ISSUED_AT)
  private long issuedAt;

  @JsonProperty(value = Claims.ISSUER)
  private String issuer;

  @JsonProperty(value = Claims.ID)
  private String tokenUUID;

  @JsonProperty(value = "pur")
  @JsonDeserialize(contentAs = SimpleGrantedAuthority.class)
  private Collection<GrantedAuthority> authorities;

  @JsonProperty(value = "pun")
  private String username;

  public boolean isTokenNotExpired() {
    return secondsToExpire() >= 0;
  }

  public long secondsToExpire() {
    return this.expirationTimeInSecond - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
  }
}

