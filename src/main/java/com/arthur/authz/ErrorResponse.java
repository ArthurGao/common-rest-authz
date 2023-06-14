package com.arthur.authz;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.arthur.rest.exceptions.ErrorCode;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class ErrorResponse {

  @JsonProperty("code")
  private int errorCode;

  @JsonProperty("message")
  private String message;

  public static ErrorResponse from(@NonNull ErrorCode errorCode) {
    return ErrorResponse.builder()
        .errorCode(errorCode.getErrorCode())
        .message(errorCode.getMessage())
        .build();
  }

}
