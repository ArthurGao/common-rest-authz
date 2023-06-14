package com.arthur.authz.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleGrantedAuthority implements GrantedAuthority {

  @JsonProperty
  private String authority;

  @Override
  public String getAuthority() {
    return authority;
  }
}
