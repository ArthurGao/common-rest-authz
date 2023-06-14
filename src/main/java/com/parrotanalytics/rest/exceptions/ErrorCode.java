package com.arthur.rest.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorCode {

  private int errorCode;
  private String message;

}
