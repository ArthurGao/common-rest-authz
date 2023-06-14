package com.arthur.authz.resolvers;

import static org.assertj.core.api.Assertions.assertThat;

import com.arthur.authz.resolvers.enums.ProductId;
import com.arthur.authz.resolvers.enums.RoleId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PermissionResolverCustomConfigurationContext.class)
class LiteUserPermissionResolverITest {

  private static final Integer MONITOR_ACCOUNT_ID = 1068;

  @Autowired
  private PermissionResolver permissionResolver;

  @Test
  void testResolveMovieLite_givenProductMovieLiveViewer_returnMovieLite() {
    List<Integer> productIds = List.of(ProductId.MOVIE_LITE_VIEWER.getId());
    List<Integer> roleIds = List.of();
    Integer userAccountId = 100;

    Set<String> actual = permissionResolver.resolve(userAccountId, productIds, roleIds);

    assertThat(actual).containsAll(List.of("D360:Movie:Lite"));
  }

  @Test
  void testResolveMovieLite_givenMovieLiveViewer_returnMovieLite() {
    List<Integer> productIds = List.of();
    List<Integer> roleIds = List.of(RoleId.MOVIE_LITE_VIEWER.getId());
    Integer userAccountId = 100;

    Set<String> actual = permissionResolver.resolve(userAccountId, productIds, roleIds);

    assertThat(actual).containsAll(List.of("D360:Movie:Lite"));
  }

  @Test
  void testResolveTalentLite_givenProductTalentLiveViewer_returnTalentLite() {
    List<Integer> productIds = List.of(ProductId.TALENT_LITE_VIEWER.getId());
    List<Integer> roleIds = List.of();
    Integer userAccountId = 100;

    Set<String> actual = permissionResolver.resolve(userAccountId, productIds, roleIds);

    assertThat(actual).containsAll(List.of("D360:Talent:Lite"));
  }

  @Test
  void testResolveTalentLite_givenRoleTalentLiveViewer_returnTalentLite() {
    List<Integer> productIds = List.of();
    List<Integer> roleIds = List.of(RoleId.TALENT_LITE_VIEWER.getId());
    Integer userAccountId = 100;

    Set<String> actual = permissionResolver.resolve(userAccountId, productIds, roleIds);

    assertThat(actual).containsAll(List.of("D360:Talent:Lite"));
  }

  @Test
  void testResolveTVLite_givenProductNotDemandPortal_returnTVLite() {
    List<Integer> productIds = List.of(ProductId.TV_DEMAND_API_AND_CUSTOM_DATA_EXPORT.getId());
    List<Integer> roleIds = List.of();
    Integer userAccountId = 100;

    Set<String> actual = permissionResolver.resolve(userAccountId, productIds, roleIds);

    assertThat(actual).containsAll(List.of("D360:TV:Lite"));
  }

  @Test
  void testResolveTVLite_givenOnlyMonitorRole_returnTVLite() {
    List<Integer> productIds = List.of();
    List<Integer> roleIds = List.of(RoleId.MONITOR_VIEWER.getId());
    Integer userAccountId = 100;

    Set<String> actual = permissionResolver.resolve(userAccountId, productIds, roleIds);

    assertThat(actual).containsAll(List.of("D360:TV:Lite"));
  }

  @Test
  void testResolveTVLite_givenUserAccountID1068_doNotReturnTVLite() {
    List<Integer> productIds = List.of(ProductId.DEMAND_PORTAL.getId());
    List<Integer> roleIds = List.of();
    Integer userAccountId = MONITOR_ACCOUNT_ID;

    Set<String> actual = permissionResolver.resolve(userAccountId, productIds, roleIds);

    assertThat(actual).isNotEmpty()
                      .contains("D360:TV:Lite");
  }

  @Test
  void testResolveTVLite_givenMonitorRoleAndEnterpriseViewer_doNotReturnTVLite() {
    List<Integer> productIds = List.of(ProductId.DEMAND_PORTAL.getId());
    List<Integer> roleIds = List.of(RoleId.MONITOR_VIEWER.getId(), RoleId.ENTERPRISE_VIEWER.getId());
    Integer userAccountId = 100;

    Set<String> actual = permissionResolver.resolve(userAccountId, productIds, roleIds);

    assertThat(actual).isNotEmpty()
                      .doesNotContain("D360:TV:Lite");
  }

  @Test
  void testResolveTVLite_givenMonitorRoleAndEnterpriseAdmin_doNotReturnTVLite() {
    List<Integer> productIds = List.of(ProductId.DEMAND_PORTAL.getId());
    List<Integer> roleIds = List.of(RoleId.MONITOR_VIEWER.getId(), RoleId.ENTERPRISE_ADMIN.getId());
    Integer userAccountId = 100;

    Set<String> actual = permissionResolver.resolve(userAccountId, productIds, roleIds);

    assertThat(actual).isNotEmpty()
                      .doesNotContain("D360:TV:Lite");
  }
}