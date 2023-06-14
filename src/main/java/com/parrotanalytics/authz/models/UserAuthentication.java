package com.arthur.authz.models;

import io.jsonwebtoken.lang.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
@EqualsAndHashCode
@ToString
public class UserAuthentication implements Authentication {

  private final InternalUserClaim claims;

  private final User springUser;

  private boolean isAuthenticated = true;

  public UserAuthentication(InternalUserClaim claims) {

    Objects.requireNonNull(claims, "The internal claims cannot be null");

    this.claims = claims;

    Collection<GrantedAuthority> authorities = List.of();

    if (!Collections.isEmpty(claims.getPermissions())) {
      authorities = claims.getPermissions()
          .stream()
          .map(SimpleGrantedAuthority::new)
          .collect(Collectors.toList());
    }

    this.springUser = new User(
        claims.getUsername(),
        "",      //trivial string
        true,             //user enabled
        true,             //accountNotExpired
        true,             //credentialsNonExpired
        true,             //accountNonLocked
        authorities);
  }

  @Override
  public String getName() {
    return claims.getUsername();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return springUser.getAuthorities();
  }

  @Override
  public Object getCredentials() {
    return springUser.getPassword();
  }

  @Override
  public Object getDetails() {
    return springUser;
  }

  @Override
  public Object getPrincipal() {
    return springUser.getUsername();
  }

  @Override
  public boolean isAuthenticated() {
    return isAuthenticated;
  }

  @Override
  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    this.isAuthenticated = isAuthenticated;
  }
}