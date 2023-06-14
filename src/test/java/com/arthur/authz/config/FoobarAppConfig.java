package com.arthur.authz.config;

import com.arthur.authz.AuthorizationProperties;
import com.arthur.authz.CustomAuthenticationEntryPoint;
import com.arthur.authz.RedissonHelper;
import com.arthur.authz.StatelessAuthenticationFilter;
import com.arthur.authz.container.RedisExtension;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

@Configuration
@EnableAutoConfiguration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class FoobarAppConfig {

  public static final String REDIS_JWT_CACHE_KEY_NAME = RandomStringUtils.randomAlphabetic(10);
  private static final String EXTERNAL_FAKE_KEY = "ABCefg30KVRoZS1Nb3N0LVBvd2VyDnVsLUNvbXBhbnktM2Y4Yzc5ZTEtNjkwNC00MDY0LTg2YmMtMzhlMWIzZWJmZGY8";
  private static final String INTERNAL_FAKE_KEY = "dGhpcy1pcy1mYWtlLXNlY3JldC1rZXktbG9uZy1sb25nLWxvbmctNGRiZDQ2ODAtN2U1NC00NzQ5LTk4NzAtMDczNzQ4ZjNkNTU2";

  @Bean
  public AuthorizationProperties authorizationProperties() {
    return AuthorizationProperties.builder()
        .externalPrivateKeyEncoded64(EXTERNAL_FAKE_KEY)
        .internalPrivateKeyEncoded64(INTERNAL_FAKE_KEY)
        .redisCacheHost("localhost")
        .redisCachePort(RedisExtension.HOST_EXPOSED_PORT)
        .redisJwtCacheKeyName(REDIS_JWT_CACHE_KEY_NAME)
        .build();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, StatelessAuthenticationFilter statelessAuthenticationFilter) throws Exception {
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

  @Bean
  public RedissonClient redissonClient(AuthorizationProperties authorizationProperties) {
    try {
      return RedissonHelper.getRedissonClient(authorizationProperties);
    } catch (Exception e) {
      return null;
    }
  }
}
