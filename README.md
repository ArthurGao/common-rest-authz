# arthur-common-authz #

This library provides useful classes for

* authorization requests in the arthur Platform
* Restful error code, runtime exceptions and exception handler to mapping exception to HTTP response status
* Converter to make ENUM in path available to be Case-insensitive

## Quick Summary

### arthur Authorisation

In arthur, we are dealing with two types of JWT. Firstly, the external JWT token was designed and used long ago. It contains a `username (aka userEmailAddress)`
, and a list of Authority (either Role or Permission). The external one is still in use in the D360 portal.

Secondly, the internal JWT token is designed and used internally only. It contains username, userId, accountId, and a list of fine-grained permission which
doesn't need to expose to the public.

The internal token is stored in the Redis cache, it has the same time-to-live value as the external one.

Detail of the document can be found here: [AuthZ](https://paper.dropbox.com/doc/AuthZ--BpR8l3hHiFZtcU3UuiJ5Ouw8Ag-sNNckGHbZTv288qQSV1ap)

## arthur Authorisation Annotations:

There are several pre-defined annotations in this library, the full list is described in this table

| Name                      | Required Permission(s)                                                           |
|:--------------------------|----------------------------------------------------------------------------------|
| AllowAnyD360Enterprise    | One-of `D360:TV:Enterprise` , `D360:Talent:Enterprise` , `D360:Movie:Enterprise` |
| AllowD360MovieEnterprise  | `D360:Movie:Enterprise`                                                          |
| AllowD360TalentEnterprise | `D360:Talent:Enterprise`                                                         |
| AllowD360TVEnterprise     | `D360:Movie:Enterprise`                                                          |

### Runtime exception handler

Can throw any Java runtime exceptions or customized runtime exceptions at any service. This base class `RestfulAPIExceptionHandlerBase` provides an
@ExceptionHandler method for handling exceptions above. This method returns a ResponseEntity for writing to the response.

### Enum converter

`EnumToStringConverterFactory` is a converter factory for "ranged" converters that can convert objects from Enum to subtypes of String. This will resolve
problem of Enum defined in path URL available must be upper-case.

### How do I get set up?

This library supports the Spring application only.

Include the dependency:

```
    <dependency>
      <groupId>com.arthur</groupId>
      <artifactId>arthur-common-authz</artifactId>
      <version>${arthur-common-authz.version}</version>
    </dependency>
```

#### Authorisation

Integration steps:

1. Define Spring @Bean Define the `AuthorizationProperties` which contain Redis host connection and private keys:

   ```java
   @Bean
   public AuthorizationProperties authorizationProperties() {
   return AuthorizationProperties.builder()
              .externalPrivateKeyEncoded64(PrivateKeyOfExternalToken)
              .internalPrivateKeyEncoded64(PrivateKeyOfInternalToken)
              .redisCacheHost(REDIS_HOST)
              .redisCachePort(REDIS_PORT)
              .redisJwtCacheKeyName(CACHE_KEY_NAME) //should be same value as defined in authN service (demand-api)
           .build();
   }
   ```

   Need to add the following configuration in the Spring-boot application class
   ```java
   @EnableScheduling
   @EnableJpaRepositories(basePackages = {"com.arthur.authz.repository"})
   @EntityScan(basePackages = {"com.arthur.authz.repository.model"})
   @ComponentScan(basePackages = {"com.arthur"})
   public class Application {}
   ```

   We have a fallback mechanism that talks to the MySQL DB if Redis is down, so the datasource for the mysql DB must be defined.

   For example:

   ```
   spring:
     datasource:
       url: "jdbc:mysql://${APP_MYSQL_HOST_URL:localhost}:${APP_MYSQL_PORT:3306}/"
       username: ${APP_MYSQL_USER_NAME:dbusername}
       password: ${APP_MYSQL_PASSWORD:password}
       driver-class-name: com.mysql.cj.jdbc.Driver
     jpa:
       database-platform: org.hibernate.dialect.MySQLDialect
       hibernate:
         naming:
           physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
   ```

2. Enable Web Security

   Add custom configuration to enable the web security:
   ```java 
   @Configuration
   @EnableAutoConfiguration
   @EnableWebSecurity
   @EnableGlobalMethodSecurity(prePostEnabled = true)
   public class SampleSecurityConfig {
   
   @Bean
   public SecurityFilterChain filterChain(
        final HttpSecurity http,
        final StatelessAuthenticationFilter statelessAuthenticationFilter) throws Exception {
   
        http.csrf().disable()
        .exceptionHandling()
            .authenticationEntryPoint(new CustomAuthenticationEntryPoint()).and()
        .servletApi().and()
        .headers()
            .cacheControl().and().and()
        .anonymous().and()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
        .addFilterBefore(statelessAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeRequests()
        .anyRequest().authenticated();
   
    return http.build();
   }
   ```
   As you see in the sample configuration, we defined the `StatelessAuthenticationFilter` to handle the jwt processing, if it failed to handle the given
   request, the request will be propagated to `CustomAuthenticationEntryPoint`.

   If there is no `RedissonClient` in the project, create it as shown below
   ```java
   @Bean
   public RedissonClient redissonClient(AuthorizationProperties authorizationProperties) {
   return getRedissonClient(authorizationProperties);
   }
   ```


3. Catch AccessDeniedException

   When the incoming request has a valid token but doesn't meet the required permission, the AccessDeniedException will be thrown. In your `@ControllerAdvice`
   class, you can define like below to catch this exception:

   ```java
   @ExceptionHandler(AccessDeniedException.class)
   private ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
     log.warn("Access denied for request {} due to {}.", request.getContextPath(), ex.getMessage(), ex);
     return ResponseEntity.status(HttpStatus.FORBIDDEN)
       .body(toJSON(ErrorResponse.from(ErrorCode.NOT_AUTHORIZED)));
   }
   ```


4. Custom Response body of error request (Optional)
   The default response body of `CustomAuthenticationEntryPoint` is defined as below:

   ```json
   { "code": 401, "message": "Authentication failed"}
   ```
   You can define your implementation by implementing the interface  `AuthenticationEntryPoint`

5. Add Annotation to your Controller

   In your Controller, you can annotate the public endpoint with the pre-defined annotations in `com.arthur.authz.annotations`  such as:
   ```java
   @GetMapping("allowD360MovieEnterprise")
   @AllowD360MovieEnterprise
   public ResponseEntity<UserAuthentication> allowD360MovieEnterprise() {
        ...
   ```

#### ES256 security

Authz service supports ES256 security. You can just use JwtES256Utils.java as a utility class.

```java
       JwtES256Utils jwtES256Utils=new JwtES256Utils();
```

1. Decode from JWT (without signature)
    - Decode the JWT token to a Map
    ```java
        Map<String, Object> result = jwtES256Utils.decodeTokenClaims(token, Map.class);
    ```
   Then can get the payload from the result map.
    - Decode the JWT token to a customized Object
    ```java
         @Data
         public class ClaimEntity {
         
         @JsonProperty(value = "internal_user_id")
         private long internalUserId;
         
         @JsonProperty(value = "internal_account_id")
         private long internalAccountId;
         
         @JsonProperty(value = "account_name")
         private String accountName;
         
         @JsonProperty(value = "email")
         private String email;
         }    
   ```
   Then
   ```java
       ClaimEntity result = jwtES256Utils.decodeTokenClaims(token, ClaimEntity.class);
   ```
   Then can get the payload from the result object.
2. Validate and parse JWT token
    - Parse the JWT token to a Map
   ```java
     PublicKey publicKey = jwtES256Utils.getECPublicKey("key/ec_public.pem");
     Map<String, Object> result = jwtES256Utils.parseAccessJwtToken(token, publicKey, Map.class);
   ```
   Then can get the payload from the result map.
    - Decode the JWT token to a customized Object
   ```java
       @Data
       public class ClaimEntity {
       
         @JsonProperty(value = "internal_user_id")
         private long internalUserId;
       
         @JsonProperty(value = "internal_account_id")
         private long internalAccountId;
       
         @JsonProperty(value = "account_name")
         private String accountName;
       
         @JsonProperty(value = "email")
         private String email;
       }
   ```
   ```java
     PublicKey publicKey = jwtES256Utils.getECPublicKey("key/ec_public.pem");
     ClaimEntity result = jwtES256Utils.parseAccessJwtToken(token, publicKey, ClaimEntity.class);
   ```
   Then can get the payload from the result object.

#### Runtime exception handler

Default error code

| Name                    | Message                                                                  | Code  |
|:------------------------|--------------------------------------------------------------------------|-------|
| BAD_REQUEST             | One or more of the arguments are invalid.                                | 40000 |
| INTERNAL_ERROR          | Could not process the request. Please try again later.                   | 50000 |
| AUTHENTICATION_FAILED   | Authentication failed.                                                   | 40100 |
| NOT_AUTHORIZED          | Not authorized to perform the operation.                                 | 40300 |
| RESOURCE_NOT_FOUND      | Resource not found.                                                      | 40400 |
| FORBIDDEN_NO_PERMISSION | Not authorized to access the requested resource(s).                      | 40301 |
| FORBIDDEN_TOKEN_EXPIRED | Not authorized to access the requested resource(s) because token expiry. | 40302 |

1. Define customized APIErrorCode if needed

````java
public interface ApiErrorCode extends ApiErrorCodeBase {

  ErrorCode CUSTOMIZED_BAD_REQUEST = new ErrorCode(505, "Customized bad request");
}
````

2. Defined customized Exception

```java
import lombok.Data;

@Data
public class CustomizedException extends RuntimeException {

  private final ErrorCode errorCode;

  public CustomizedException(String msg) {
    super(msg);
    this.errorCode = ApiErrorCode.CUSTOMIZED_BAD_REQUEST;
  }

  public CustomizedException(String msg, ErrorCode errorCode) {
    super(msg);
    this.errorCode = errorCode;
    this.errorCode.setMessage(msg);
  }
}
```

3. Create a `ExceptionHandler` extends `RestfulAPIExceptionHandlerBase`
    - a. Add method to handle customized exception
    - b. Implement `createErrorResponse` method and return actual Object to be put into HTTP response (Most time, it is StopLight ErrorResponseDto)

```java

@Log4j2
@ControllerAdvice
public class CustomizedExceptionHandler extends RestfulAPIExceptionHandlerBase<ErrorResponseDto> {

  @ExceptionHandler(CustomizedException.class)
  private ResponseEntity<ErrorResponseDto> handleCustomizedException(
      CustomizedException ex, WebRequest request) {
    log.warn("Illegal customized exception for request {} due to {}.", request.getContextPath(),
        ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.HTTP_VERSION_NOT_SUPPORTED)
        .body(createErrorResponse(ex.getErrorCode()));
  }

  @Override
  public ErrorResponseDto createErrorResponse(ErrorCode apiErrorCode) {
    ErrorResponseDto errorResponseDto = new ErrorResponseDto();
    errorResponseDto.setCode(apiErrorCode.getErrorCode());
    errorResponseDto.setMessage(apiErrorCode.getMessage());
    return errorResponseDto;
  }
}
```

#### Enum converter

Add `com.arthur.rest` to SpringBoot application @ComponentScan

```java

@SpringBootApplication
@Log4j2
@ConfigurationPropertiesScan
@ComponentScan(basePackages = {"com.arthur.rest"})

public class XXXXApplication {

}

```
