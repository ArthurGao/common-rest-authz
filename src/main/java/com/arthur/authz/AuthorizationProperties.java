package com.arthur.authz;

import lombok.Builder;
import lombok.Getter;

/**
 * A class contains Authorization properties configuration
 */
@Builder
@Getter
public class AuthorizationProperties {

  private String jwtInternalTokenIssuer;

  /**
   * Private key of external jwt encoded as base64 string
   */
  private String externalPrivateKeyEncoded64;

  /**
   * Private key of internal jwt encoded as base64 string
   */
  private String internalPrivateKeyEncoded64;

  /**
   * Internal cache store name which we store the internal jwt
   */
  private String redisJwtCacheKeyName;

  /**
   * redis host
   */
  private String redisCacheHost;

  /**
   * Redis port
   */
  private int redisCachePort;
}
