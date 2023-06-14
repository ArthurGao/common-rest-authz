package com.arthur.authz.resolvers;

import com.arthur.authz.resolvers.enums.ProductId;
import com.arthur.authz.resolvers.enums.RoleId;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

@Component
@NoArgsConstructor
public class AudienceInsightsPermissionResolver implements PermissionCoreResolver {

  private static final String MODULE_NAME = "AudienceInsights";

  private static final String ACCESS_TYPE = "Data";

  @Override
  public Set<String> resolvePermissionByProductIds(@NonNull List<Integer> productIds) {
    Set<String> result = new HashSet<>();

    if (productIds.contains(ProductId.AUDIENCE_INSIGHTS_MODULE.getId())) {

      if (productIds.contains(ProductId.DEMAND_PORTAL.getId())) {
        result.add(format(PERMISSION_PATTERN, MODULE_NAME, ENTITY_TV_NAME, ACCESS_TYPE));
      }
      if (productIds.contains(ProductId.TALENT_ENTERPRISE_MODULE.getId())) {
        result.add(format(PERMISSION_PATTERN, MODULE_NAME, ENTITY_TALENT_NAME, ACCESS_TYPE));
      }
      if (productIds.contains(ProductId.MOVIE_ENTERPRISE_MODULE.getId())) {
        result.add(format(PERMISSION_PATTERN, MODULE_NAME, ENTITY_MOVIE_NAME, ACCESS_TYPE));
      }
    }
    return result;
  }

  @Override
  public Set<String> resolvePermissionByRoleIds(@NonNull List<Integer> roleIds) {
    Set<String> result = new HashSet<>();

    if (roleIds.contains(RoleId.AUDIENCE_INSIGHTS_MODULE_ACCESS.getId())) {
      if (roleIds.contains(RoleId.ENTERPRISE_VIEWER.getId())) {
        result.add(format(PERMISSION_PATTERN, MODULE_NAME, ENTITY_TV_NAME, ACCESS_TYPE));
      }

      if (roleIds.contains(RoleId.TALENT_ENTERPRISE_MODULE.getId())) {
        result.add(format(PERMISSION_PATTERN, MODULE_NAME, ENTITY_TALENT_NAME, ACCESS_TYPE));
      }
      if (roleIds.contains(RoleId.MOVIE_ENTERPRISE_MODULE.getId())) {
        result.add(format(PERMISSION_PATTERN, MODULE_NAME, ENTITY_MOVIE_NAME, ACCESS_TYPE));
      }
    }
    return result;
  }

}
