package com.arthur.authz.resolvers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PermissionResolver {

  private final List<PermissionCoreResolver> resolverImplementations;

  @Autowired
  private PermissionResolver(List<PermissionCoreResolver> resolverImplementations) {

    this.resolverImplementations = resolverImplementations;
  }

  /**
   * Mapping a list of roleId and productId into list of unique permission
   *
   * @param productIds
   * @param rolesId
   * @return
   */
  public Set<String> resolve(final Integer userAccountId,
                             final List<Integer> productIds,
                             final List<Integer> rolesId) {

    Set<String> result = new HashSet<>();

    resolverImplementations.forEach(
        coreResolver -> result.addAll(coreResolver.resolve(userAccountId, productIds, rolesId))
    );

    return result;
  }
}
