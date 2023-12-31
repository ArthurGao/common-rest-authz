package com.arthur.authz.resolvers.enums;

/**
 * All roleId in our system
 */
public enum RoleId {

  PORTAL_ADMIN(1),
  PORTAL_VIEWER(2),
  HELPDESK_VIEWER(3),
  HELPDESK_ADMIN(4),
  SUPPORT_ACCOUNT(5),
  arthur_ADMIN(6),
  HELPDESK_TITLE_CREATOR(7),
  BRAND_AFFINITY_ACCESS(8),
  HELPDESK_TITLE_NAME_CHANGE(9),
  MONITOR_VIEWER(10),
  ENTERPRISE_VIEWER(11),
  ENTERPRISE_ADMIN(12),
  HELPDESK_ACCOUNT_MANAGEMENT(13),
  TALENT_LITE_VIEWER(14),
  TALENT_ENTERPRISE_MODULE(15),
  MOVIE_LITE_VIEWER(16),
  MOVIE_ENTERPRISE_MODULE(17),
  @Deprecated
  EXTERNAL_API(100),
  @Deprecated
  MOVIE_CUSTOM_REPORT_ACCESS(110),
  AUDIENCE_INSIGHTS_MODULE_ACCESS(111),
  TV_DEMAND_API_AND_CUSTOM_DATA_EXPORT(112),
  MOVIE_DEMAND_API_AND_CUSTOM_DATA_EXPORT(113),
  TALENT_DEMAND_API_AND_CUSTOM_DATA_EXPORT(114);

  private final int id;

  RoleId(Integer id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }
}
