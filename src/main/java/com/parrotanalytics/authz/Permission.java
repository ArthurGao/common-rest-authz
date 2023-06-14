package com.arthur.authz;

public enum Permission {
  D360_TV_ENTERPRISE("D360:TV:Enterprise"),
  D360_TV_LITE("D360:TV:Lite"),
  D360_TALENT_ENTERPRISE("D360:Talent:Enterprise"),
  D360_TALENT_LITE("D360:Talent:Lite"),
  D360_MOVIE_ENTERPRISE("D360:Movie:Enterprise"),
  D360_MOVIE_LITE("D360:Movie:Lite"),

  D360_MOVIE_DEMAND_AND_EXPORT("D360:Movie:DemandDataAPIAndDataExport"),
  D360_TALENT_DEMAND_AND_EXPORT("D360:Talent:DemandDataAPIAndDataExport"),
  D360_TV_DEMAND_AND_EXPORT("D360:TV:DemandDataAPIAndDataExport")
  ;

  String value;

  Permission(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
