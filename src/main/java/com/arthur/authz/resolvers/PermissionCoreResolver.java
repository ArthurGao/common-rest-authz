package com.arthur.authz.resolvers;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface PermissionCoreResolver {

  String PERMISSION_PATTERN = "%s:%s:%s";

  String ENTITY_TALENT_NAME = "Talent";
  String ENTITY_MOVIE_NAME = "Movie";
  String ENTITY_TV_NAME = "TV";

  /**
   * Return a list of permission determined by given ProductIds and RoleIds
   * @param productIds
   * @param roleIds
   * @return
   */
  default Set<String> resolve(Integer accountId, List<Integer> productIds, List<Integer> roleIds) {
    Set<String> result = new HashSet<>();
    result.addAll(resolvePermissionByProductIds(productIds));
    result.addAll(resolvePermissionByRoleIds(roleIds));
    result.addAll(resolvePermissionByAccountId(accountId));
    return result;
  }

  /**
   * Return a list of permission determined by given ProductIds
   * @param productIds
   * @return
   */
  Set<String> resolvePermissionByProductIds(List<Integer> productIds);

  /**
   * Return a list of permission determined by given RoleIds
   * @param roleIds
   * @return
   */
  Set<String> resolvePermissionByRoleIds(List<Integer> roleIds);

  /**
   * Return a list of permission determined by given AccountId
   * @param accountId
   * @return
   */
  default Set<String> resolvePermissionByAccountId(Integer accountId){
    return Collections.emptySet();
  }

  default String constructPermissionBy(String module, String entity, String accessType){
    return String.format(PERMISSION_PATTERN, module, entity, accessType);
  }

}
