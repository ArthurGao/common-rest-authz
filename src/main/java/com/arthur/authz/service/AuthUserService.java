package com.arthur.authz.service;

import com.arthur.authz.AuthorizationProperties;
import com.arthur.authz.models.InternalUserClaim;
import com.arthur.authz.repository.AuthAccountRepository;
import com.arthur.authz.repository.AuthProductSpecRepository;
import com.arthur.authz.repository.AuthUserRepository;
import com.arthur.authz.repository.model.*;
import com.arthur.authz.resolvers.PermissionResolver;
import com.arthur.authz.util.MaskUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Log4j2
public class AuthUserService {
    private static final int EXPIRY_TIMEOUT_SECONDS = 28800;

    private final AuthUserRepository authUserRepository;
    private final AuthProductSpecRepository authProductSpecRepository;
    private final AuthAccountRepository authAccountRepository;
    private final PermissionResolver permissionResolver;
    private final AuthorizationProperties authorizationProperties;

    @Autowired
    public AuthUserService(AuthUserRepository authUserRepository, AuthProductSpecRepository authProductSpecRepository, AuthAccountRepository authAccountRepository, PermissionResolver permissionResolver, AuthorizationProperties authorizationProperties) {
        this.authUserRepository = authUserRepository;
        this.authProductSpecRepository = authProductSpecRepository;
        this.authAccountRepository = authAccountRepository;
        this.permissionResolver = permissionResolver;
        this.authorizationProperties = authorizationProperties;
    }

    public Optional<InternalUserClaim> getInternalUserClaimFromEmailAddress(String emailAddress) {
        AuthUser user = authUserRepository.findUserByEmailAddress(emailAddress);

        if (user == null) {
            return Optional.empty();
        }

        return getInternalUserClaim(user);
    }

    public Optional<InternalUserClaim> getInternalUserClaimFromApiKey(String apiKey) {
        AuthAccount authAccount = authAccountRepository.findAccountByApiId(apiKey);
        if(authAccount == null) {
            log.warn("There was no account for the API key {}", MaskUtil.mask(apiKey));
            return Optional.empty();
        }

        AuthUser user = authUserRepository.findUserByAccountId(authAccount.getIdAccount());
        if(user == null) {
            log.warn("There was no appropriate user for the API key {}", MaskUtil.mask(apiKey));
            return Optional.empty();
        }

        return getInternalUserClaim(user);

    }

    private Optional<InternalUserClaim> getInternalUserClaim(AuthUser user) {
        List<Integer> productIds = authProductSpecRepository.getProductIdsByIdAccount(user.getIdAccount());

        List<Integer> roleIds = user.getUserRoles()
                .stream()
                .map(AuthUserRole::getIdRole)
                .collect(Collectors.toList());

        List<Integer> userPermissionProductIds = user.getAuthUserPermissions()
                .stream()
                .map(AuthUserPermissions::getProductPermissions)
                .map(AuthProductPermissions::getProductId)
                .collect(Collectors.toList());

        productIds.addAll(userPermissionProductIds);

        Set<String> permissions = permissionResolver.resolve(user.getIdAccount(), productIds, roleIds);

        long currentTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        return Optional.of(InternalUserClaim.builder()
                .username(user.getEmailAddress())
                .userId(user.getIdUser())
                .accountId(user.getIdAccount())
                .tokenUUID(UUID.randomUUID().toString())
                .issuedAt(currentTimeInSeconds)
                .expirationTimeInSecond(currentTimeInSeconds + EXPIRY_TIMEOUT_SECONDS)
                .issuer(authorizationProperties.getJwtInternalTokenIssuer())
                .permissions(permissions)
                .build());
    }
}
