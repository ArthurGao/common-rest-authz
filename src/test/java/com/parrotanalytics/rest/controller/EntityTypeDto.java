package com.arthur.rest.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EntityTypeDto {
  SHOW("show"),

  MOVIE("movie"),

  TALENT("talent");

  private String value;

  EntityTypeDto(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }
  
  @JsonCreator
  public static EntityTypeDto fromValue(String value) {
    for (EntityTypeDto b : EntityTypeDto.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}
