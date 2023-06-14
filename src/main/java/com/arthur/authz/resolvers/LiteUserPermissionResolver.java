package com.arthur.authz.resolvers;

import static java.lang.String.format;

import com.arthur.authz.resolvers.enums.ProductId;
import com.arthur.authz.resolvers.enums.RoleId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class LiteUserPermissionResolver implements PermissionCoreResolver {

  private static final String MODULE_NAME = "D360";

  private static final String ACCESS_TYPE = "Lite";

  public static final Integer MONITOR_ACCOUNT_ID = 1068;

  @Override
  public Set<String> resolvePermissionByProductIds(@NonNull List<Integer> productIds) {
    Set<String> result = new HashSet<>();

    if (productIds.contains(ProductId.TALENT_LITE_VIEWER.getId())) {
      result.add(format(PERMISSION_PATTERN, MODULE_NAME, ENTITY_TALENT_NAME, ACCESS_TYPE));
    }

    if (productIds.contains(ProductId.MOVIE_LITE_VIEWER.getId())) {
      result.add(format(PERMISSION_PATTERN, MODULE_NAME, ENTITY_MOVIE_NAME, ACCESS_TYPE));
    }

    if (!productIds.contains(ProductId.DEMAND_PORTAL.getId())) {
      result.add(format(PERMISSION_PATTERN, MODULE_NAME, ENTITY_TV_NAME, ACCESS_TYPE));
    }

    return result;
  }

  @Override
  public Set<String> resolvePermissionByRoleIds(@NonNull List<Integer> roleIds) {
    Set<String> result = new HashSet<>();

    if (roleIds.contains(RoleId.TALENT_LITE_VIEWER.getId())) {
      result.add(format(PERMISSION_PATTERN, MODULE_NAME, ENTITY_TALENT_NAME, ACCESS_TYPE));
    }

    if (roleIds.contains(RoleId.MOVIE_LITE_VIEWER.getId())) {
      result.add(format(PERMISSION_PATTERN, MODULE_NAME, ENTITY_MOVIE_NAME, ACCESS_TYPE));
    }

    if (hasTvLiteRole(roleIds)) {
      result.add(format(PERMISSION_PATTERN, MODULE_NAME, ENTITY_TV_NAME, ACCESS_TYPE));
    }

    return result;
  }

  @Override
  public Set<String> resolvePermissionByAccountId(Integer accountId) {
    if (MONITOR_ACCOUNT_ID.equals(accountId)) {
      return Set.of(format(PERMISSION_PATTERN, MODULE_NAME, ENTITY_TV_NAME, ACCESS_TYPE));
    }
    return Set.of();
  }

  /**
   * TvLite user has role id 10 (Monitor Viewer) and does not have role 11 or 12
   *
   * @param roleIds
   * @return
   */
  private boolean hasTvLiteRole(List<Integer> roleIds) {
    return roleIds.contains(RoleId.MONITOR_VIEWER.getId()) &&
        (!roleIds.contains(RoleId.ENTERPRISE_VIEWER.getId()) &&
            !roleIds.contains(RoleId.ENTERPRISE_ADMIN.getId()));
  }
}
