package com.arthur.authz.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

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
