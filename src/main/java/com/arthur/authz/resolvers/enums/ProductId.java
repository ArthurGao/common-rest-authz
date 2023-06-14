package com.arthur.authz.resolvers.enums;

/**
 * All productId in our system
 */
public enum ProductId {

  DEMAND_PORTAL(1),
  HISTORICAL_DATA_ACCESS(2),
  GLOBAL_TRENDS(3),
  TOPCONTENT_MODULE(4),
  DEMAND_BREAKDOWN_MODULE(5),
  @Deprecated
  EXTERNAL_API(6),
  TALENT_ENTERPRISE_MODULE(7),
  TALENT_LITE_VIEWER(8),
  MOVIE_ENTERPRISE_MODULE(9),
  MOVIE_LITE_VIEWER(10),
  @Deprecated
  MOVIE_CUSTOM_REPORT_MODULE(11),
  AUDIENCE_INSIGHTS_MODULE(12),
  CMS(13),
  TV_DEMAND_API_AND_CUSTOM_DATA_EXPORT(14),
  MOVIE_DEMAND_API_AND_CUSTOM_DATA_EXPORT(15),
  TALENT_DEMAND_API_AND_CUSTOM_DATA_EXPORT(16);

  private final int id;

  ProductId(Integer productId) {
    this.id = productId;
  }

  public int getId() {
    return id;
  }
}
