package com.arthur.rest.controller;

import com.arthur.rest.exceptions.ApiErrorCode;
import com.arthur.rest.exceptions.BadRequestException;
import com.arthur.rest.exceptions.CustomizedException;
import com.arthur.rest.exceptions.ForbiddenException;
import com.arthur.rest.exceptions.ServerInternalException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestTestController {

  public static final String RESULT_REQUEST = "TestResult_request";

  @RequestMapping(
      method = RequestMethod.GET,
      value = "/testrequest/{entityType}",
      produces = {"application/json"}
  )
  @PreAuthorize("hasAuthority('CAN_ACCESS')")
  public String testRequest(@PathVariable("entityType") EntityTypeDto entityType) {
    return RESULT_REQUEST;
  }


  @GetMapping("testBadRequestException")
  @PreAuthorize("hasAuthority('CAN_ACCESS')")
  public String testBadRequestException() {
    throw new BadRequestException("BadRequestException message");
  }

  @GetMapping("testCustomizedBadRequestException")
  @PreAuthorize("hasAuthority('CAN_ACCESS')")
  public String testCustomizedBadRequestException() {
    throw new BadRequestException(ApiErrorCode.CUSTOMIZED_BAD_REQUEST, "Customized bad request message");
  }

  @GetMapping("testIllegalArgumentException")
  @PreAuthorize("hasAuthority('CAN_ACCESS')")
  public String testIllegalArgumentException() {
    throw new IllegalArgumentException("IllegalArgument Exception");
  }

  @GetMapping("testServerInternalException")
  @PreAuthorize("hasAuthority('CAN_ACCESS')")
  public String testServerInternalException() {
    throw new ServerInternalException("ServerInternalException message");
  }

  @GetMapping("testForbiddenException")
  @PreAuthorize("hasAuthority('CAN_ACCESS')")
  public String testForbiddenException() {
    throw new ForbiddenException("ForbiddenException message");
  }


  @GetMapping("testCustomizedException")
  @PreAuthorize("hasAuthority('CAN_ACCESS')")
  public String testCustomizedException() {
    throw new CustomizedException(ApiErrorCode.CUSTOMIZED_BAD_REQUEST);
  }
}
