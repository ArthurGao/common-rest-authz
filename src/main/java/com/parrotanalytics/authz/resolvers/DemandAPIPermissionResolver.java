package com.arthur.authz.resolvers;

import com.arthur.authz.resolvers.enums.ProductId;
import com.arthur.authz.resolvers.enums.RoleId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolver for resolve public api access and custom data export
 */
@Component
@NoArgsConstructor
@Slf4j
public class DemandAPIPermissionResolver implements PermissionCoreResolver {

  private static final String MODULE_NAME = "D360";

  private static final String ACCESS_TYPE = "DemandDataAPIAndDataExport";

  @Override
  public Set<String> resolvePermissionByProductIds(@NonNull List<Integer> productIds) {
    Set<String> result = new HashSet<>();

    // Deprecated product, will be removed after finish move to new product
    if (productIds.contains(ProductId.EXTERNAL_API.getId())) {
      result.add(constructPermissionBy(MODULE_NAME, ENTITY_TV_NAME, ACCESS_TYPE));
    }

    if (productIds.contains(ProductId.MOVIE_CUSTOM_REPORT_MODULE.getId())) {
      result.add(constructPermissionBy(MODULE_NAME, ENTITY_MOVIE_NAME, ACCESS_TYPE));
    }
    // END

    if (productIds.contains(ProductId.TV_DEMAND_API_AND_CUSTOM_DATA_EXPORT.getId())){
      result.add(constructPermissionBy(MODULE_NAME, ENTITY_TV_NAME, ACCESS_TYPE));
    }

    if (productIds.contains(ProductId.MOVIE_DEMAND_API_AND_CUSTOM_DATA_EXPORT.getId())) {
      result.add(constructPermissionBy(MODULE_NAME, ENTITY_MOVIE_NAME, ACCESS_TYPE));
    }

    if (productIds.contains(ProductId.TALENT_DEMAND_API_AND_CUSTOM_DATA_EXPORT.getId())) {
      result.add(constructPermissionBy(MODULE_NAME, ENTITY_TALENT_NAME, ACCESS_TYPE));
    }

    return result;
  }

  @Override
  public Set<String> resolvePermissionByRoleIds(@NonNull List<Integer> roleIds) {
    Set<String> result = new HashSet<>();

    // Deprecated product, will be removed after finish move to new product
    if (roleIds.contains(RoleId.EXTERNAL_API.getId())) {
      result.add(constructPermissionBy(MODULE_NAME, ENTITY_TV_NAME, ACCESS_TYPE));
    }

    if (roleIds.contains(RoleId.MOVIE_CUSTOM_REPORT_ACCESS.getId())) {
      result.add(constructPermissionBy(MODULE_NAME, ENTITY_MOVIE_NAME, ACCESS_TYPE));
    }
    // END

    if (roleIds.contains(RoleId.TV_DEMAND_API_AND_CUSTOM_DATA_EXPORT.getId())){
      result.add(constructPermissionBy(MODULE_NAME, ENTITY_TV_NAME, ACCESS_TYPE));
    }

    if (roleIds.contains(RoleId.MOVIE_DEMAND_API_AND_CUSTOM_DATA_EXPORT.getId())) {
      result.add(constructPermissionBy(MODULE_NAME, ENTITY_MOVIE_NAME, ACCESS_TYPE));
    }

    if (roleIds.contains(RoleId.TALENT_DEMAND_API_AND_CUSTOM_DATA_EXPORT.getId())) {
      result.add(constructPermissionBy(MODULE_NAME, ENTITY_TALENT_NAME, ACCESS_TYPE));
    }

    return result;
  }

}
