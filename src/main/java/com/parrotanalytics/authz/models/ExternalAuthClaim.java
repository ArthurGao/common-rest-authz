package com.arthur.authz.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.arthur.authz.util.StringToScopesDeserializer;
import io.jsonwebtoken.Claims;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@Getter
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAuthClaim {
    @JsonProperty(value = "internal_user_id")
    private Long userId;
    @JsonProperty(value = "internal_account_id")
    private Long accountId;
    @JsonProperty(value = "internal_account_name")
    private String accountName;
    @JsonProperty(value = "email")
    private String email;
    @JsonProperty(value = "scope")
    @JsonDeserialize(using = StringToScopesDeserializer.class)
    private List<Scope> scopes;
    @JsonProperty(value = Claims.EXPIRATION)
    private long expirationTime;
    @JsonProperty(value = Claims.ISSUED_AT)
    private long issuedAt;
    @JsonProperty(value = Claims.ISSUER)
    private String issuer;
    @JsonProperty(value = Claims.ID)
    private String tokenUuid;

    public long millisecondsToExpire() {
        return this.expirationTime - System.currentTimeMillis();
    }
    public boolean isTokenNotExpired() {
        return millisecondsToExpire() >= 0;
    }
    public long secondsToExpire() {
        return this.expirationTime/1000;
    }

    @Data
    @Builder
    public static final class Scope implements GrantedAuthority {
        @JsonProperty(value = "scope")
        private String scopes;
        public Scope(String scopes) {
            this.scopes = scopes;
        }
        @Override
        public String getAuthority() {
            return scopes;
        }
    }
}
