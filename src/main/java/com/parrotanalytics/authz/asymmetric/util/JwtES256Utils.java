package com.arthur.authz.asymmetric.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class JwtES256Utils {

  private ObjectMapper mapper;

  private static final String arthur = "arthur Analytics";
  private static final long EXPIRE = 1000 * 60 * 60 * 24;

  @Autowired
  public JwtES256Utils() {
    this.mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public String createAccessJwtToken(Key encode64SessionSecret, Map<String, Object> claims) {
    return createAccessJwtToken(encode64SessionSecret, arthur, EXPIRE, claims);
  }

  public String createAccessJwtToken(Key encode64SessionSecret, String issuer, long expire, Map<String, Object> claims) {
    return Jwts.builder()
        .setClaims(claims)
        .setIssuer(issuer)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expire))
        .signWith(SignatureAlgorithm.ES256, encode64SessionSecret)
        .compact();
  }

  public <T> T decodeJwtToken(String token, Class<T> tClass) {
    String[] splitToken = token.split("\\.");
    String unsignedToken = splitToken[0] + "." + splitToken[1] + ".";
    Claims body = Jwts.parser().parseClaimsJwt(unsignedToken).getBody();
    return mapper.convertValue(body, tClass);
  }

  public <T> T validateAndParseJwtToken(String accessToken, PublicKey publicKey, Class<T> tClass) {
    try {
      if (StringUtils.isEmpty(accessToken)) {
        throw new RuntimeException("Jwt token is empty");
      }
      Claims body = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(accessToken).getBody();
      return mapper.convertValue(body, tClass);
    } catch (ExpiredJwtException eje) {
      throw new RuntimeException("Jwt token is expired: ", eje);
    } catch (UnsupportedJwtException uje) {
      throw new RuntimeException("Unsupported Jwt: ", uje);
    } catch (MalformedJwtException mje) {
      throw new RuntimeException("Malformed Jwt: ", mje);
    } catch (IllegalArgumentException se) {
      throw new RuntimeException("Illegal Argument Exception: " + se.getMessage(), se);
    }
  }

  public PrivateKey getECPrivateKey(InputStream privateKeyInputStream) {
    try {
      Security.addProvider(new BouncyCastleProvider());
      KeyFactory keyFactory = KeyFactory.getInstance("ECDH", "BC");
      if (privateKeyInputStream == null) {
        throw new RuntimeException("Private key file not found: " + privateKeyInputStream);
      }
      return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(IOUtils.toByteArray(privateKeyInputStream)));
    } catch (InvalidKeySpecException e) {
      throw new RuntimeException("Invalid Key Spec: ", e);
    } catch (Exception e) {
      throw new RuntimeException("Exception occurs: ", e);
    }
  }

  public PublicKey getECPublicKey(String publicFileName) {
    try {
      Security.addProvider(new BouncyCastleProvider());
      KeyFactory keyFactory = KeyFactory.getInstance("ECDH", "BC");
      InputStream inputStream = getClass().getClassLoader().getResourceAsStream(publicFileName);
      String publicKeyBase64 = IOUtils.toString(inputStream, "UTF-8");
      publicKeyBase64 = publicKeyBase64.replaceAll("\\-*BEGIN.*KEY\\-*", "")
          .replaceAll("\\-*END.*KEY\\-*", "")
          .replaceAll("\r", "")
          .replaceAll("\n", "");
      byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
      return keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
    } catch (InvalidKeySpecException e) {
      throw new RuntimeException("Invalid Key Spec: ", e);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
