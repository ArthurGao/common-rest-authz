package com.arthur.authz.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.arthur.authz.asymmetric.util.JwtES256Utils;
import java.io.InputStream;
import java.security.PublicKey;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JwtES256UtilsTest {


  private Map<String, Object> buildClaim() {
    return Map.of(
        "internal_user_id", 2089,
        "internal_account_id", 100,
        "account_name", "arthur Analytics (Sales)",
        "email", "test@parroyanaltics.com");
  }

  @Test
  public void testDecodeTokenClaimsByMap_givenWithoutKey_successful() {
    Map<String, Object> claim = buildClaim();
    JwtES256Utils jwtES256Utils = new JwtES256Utils();
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("key/ec_private_pkcs8");
    String token = jwtES256Utils.createAccessJwtToken(jwtES256Utils.getECPrivateKey(inputStream), claim);
    Map<String, Object> result = jwtES256Utils.decodeJwtToken(token, Map.class);
    assertThat(result.get("email")).isEqualTo("test@parroyanaltics.com");
    assertThat(result.get("account_name")).isEqualTo("arthur Analytics (Sales)");
    assertThat(result.get("internal_account_id")).isEqualTo(100);
    assertThat(result.get("internal_user_id")).isEqualTo(2089);
  }

  @Test
  public void testDecodeTokenClaimsByObject_givenWithoutKey_successful() {
    Map<String, Object> claim = buildClaim();
    JwtES256Utils jwtES256Utils = new JwtES256Utils();
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("key/ec_private_pkcs8");
    String token = jwtES256Utils.createAccessJwtToken(jwtES256Utils.getECPrivateKey(inputStream), claim);
    ClaimEntity result = jwtES256Utils.decodeJwtToken(token, ClaimEntity.class);
    assertThat(result.getEmail()).isEqualTo("test@parroyanaltics.com");
    assertThat(result.getAccountName()).isEqualTo("arthur Analytics (Sales)");
    assertThat(result.getInternalAccountId()).isEqualTo(100);
    assertThat(result.getInternalUserId()).isEqualTo(2089);
  }

  @Test
  public void testParseAccessJwtTokenByMap_givenCorrectPublicKey_successful() {
    Map<String, Object> claim = buildClaim();
    JwtES256Utils jwtES256Utils = new JwtES256Utils();
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("key/ec_private_pkcs8");
    String token = jwtES256Utils.createAccessJwtToken(jwtES256Utils.getECPrivateKey(inputStream), claim);
    PublicKey publicKey = jwtES256Utils.getECPublicKey("key/ec_public.pem");
    Map<String, Object> result = jwtES256Utils.validateAndParseJwtToken(token, publicKey, Map.class);
    assertThat(result.get("email")).isEqualTo("test@parroyanaltics.com");
    assertThat(result.get("account_name")).isEqualTo("arthur Analytics (Sales)");
    assertThat(result.get("internal_account_id")).isEqualTo(100);
    assertThat(result.get("internal_user_id")).isEqualTo(2089);
  }

  @Test
  public void testParseAccessJwtTokenByObject_givenCorrectPublicKey_successful() {
    Map<String, Object> claim = buildClaim();
    JwtES256Utils jwtES256Utils = new JwtES256Utils();
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("key/ec_private_pkcs8");
    String token = jwtES256Utils.createAccessJwtToken(jwtES256Utils.getECPrivateKey(inputStream), claim);
    PublicKey publicKey = jwtES256Utils.getECPublicKey("key/ec_public.pem");

    ClaimEntity result = jwtES256Utils.validateAndParseJwtToken(token, publicKey, ClaimEntity.class);
    assertThat(result.getEmail()).isEqualTo("test@parroyanaltics.com");
    assertThat(result.getAccountName()).isEqualTo("arthur Analytics (Sales)");
    assertThat(result.getInternalAccountId()).isEqualTo(100);
    assertThat(result.getInternalUserId()).isEqualTo(2089);
  }

  @Test
  public void testParseAccessJwtTokenByObject_withoutPublicKey_exception() {
    Map<String, Object> claim = buildClaim();
    JwtES256Utils jwtES256Utils = new JwtES256Utils();
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("key/ec_private_pkcs8");
    String token = jwtES256Utils.createAccessJwtToken(jwtES256Utils.getECPrivateKey(inputStream), claim);
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> {
          jwtES256Utils.validateAndParseJwtToken(token, null, ClaimEntity.class);
        }).withMessageContaining("signing key cannot be null.");
  }

  @Test
  public void testParseAccessJwtTokenByObject_givenMismatchedPublicKey_exception() {
    Map<String, Object> claim = buildClaim();
    JwtES256Utils jwtES256Utils = new JwtES256Utils();
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("key/ec_private_pkcs9");

    String token = jwtES256Utils.createAccessJwtToken(jwtES256Utils.getECPrivateKey(inputStream), claim);
    PublicKey publicKey = jwtES256Utils.getECPublicKey("key/ec_public.pem");

    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> {
          jwtES256Utils.validateAndParseJwtToken(token, publicKey, ClaimEntity.class);
        }).withMessageContaining("JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.");
  }

  @Test
  public void testGetECPublicKey_givenWrongPublicKey_getException() {
    JwtES256Utils jwtES256Utils = new JwtES256Utils();
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> {
          jwtES256Utils.getECPublicKey("key/ec_public_error.pem");
        }).withMessageContaining("Invalid Key Spec");
  }

  @Test
  public void testGetECPrivateKey_givenWrongPrivateKey_getException() {
    JwtES256Utils jwtES256Utils = new JwtES256Utils();
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("key/ec_private_wrong.pem");
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> {
          jwtES256Utils.getECPrivateKey(inputStream);
        }).withMessageContaining("Invalid Key Spec");
  }
} 
