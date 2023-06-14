package com.arthur.rest;

import com.arthur.AuthzTestBase;
import com.arthur.authz.StatelessAuthenticationFilter;
import com.arthur.authz.container.MySQLExtension;
import com.arthur.authz.container.RedisExtension;
import com.arthur.authz.helper.JwtTokenTestHelper;
import com.arthur.rest.exceptions.ApiErrorCode;
import com.arthur.rest.exceptions.DefaultAPIErrorCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith({RedisExtension.class, MySQLExtension.class, SpringExtension.class})
class RestfulITest extends AuthzTestBase {

  @Resource
  private MockMvc mockMvc;

  @ParameterizedTest
  @ValueSource(strings = {"MOVIE", "movie", "MOviE"})
  void testConverter_givenCaseInsensitiveURL_return200Status(String entity) throws Exception {
    String url = "/testrequest/" + entity;
    Set<String> permission = Set.of("CAN_ACCESS");
    String internalToken = JwtTokenTestHelper.createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permission);
    internalJwtCacheStore.put(USER_NAME, internalToken);
    this.mockMvc.perform(get(url).header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isOk());
  }

  @Test
  void testBadRequestException_givenThrowBadRequestException_return400() throws Exception {
    String url = "/testBadRequestException";
    Set<String> permission = Set.of("CAN_ACCESS");
    String internalToken = JwtTokenTestHelper.createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permission);
    internalJwtCacheStore.put(USER_NAME, internalToken);
    this.mockMvc.perform(get(url).header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(DefaultAPIErrorCodes.BAD_REQUEST.getErrorCode()))
        .andExpect(jsonPath("$.message").value("One or more of the arguments are invalid."));
  }

  @Test
  void testCustomizedBadRequestException_givenThrowCustomizedBadRequestException_return401() throws Exception {
    String url = "/testCustomizedBadRequestException";
    Set<String> permission = Set.of("CAN_ACCESS");
    String internalToken = JwtTokenTestHelper.createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permission);
    internalJwtCacheStore.put(USER_NAME, internalToken);
    this.mockMvc.perform(get(url).header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(ApiErrorCode.CUSTOMIZED_BAD_REQUEST.getErrorCode()))
        .andExpect(jsonPath("$.message").value(ApiErrorCode.CUSTOMIZED_BAD_REQUEST.getMessage()));
  }

  @Test
  void testServerInternalException_givenThrowServerInternalException_return500() throws Exception {
    String url = "/testServerInternalException";
    Set<String> permission = Set.of("CAN_ACCESS");
    String internalToken = JwtTokenTestHelper.createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permission);
    internalJwtCacheStore.put(USER_NAME, internalToken);
    this.mockMvc.perform(get(url).header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value(DefaultAPIErrorCodes.INTERNAL_ERROR.getErrorCode()))
        .andExpect(jsonPath("$.message").value("Could not process the request. Please try again later."));
  }

  @Test
  void testIllegalArgumentException_givenThrowArgumentException_return400() throws Exception {
    String url = "/testIllegalArgumentException";
    Set<String> permission = Set.of("CAN_ACCESS");
    String internalToken = JwtTokenTestHelper.createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permission);
    internalJwtCacheStore.put(USER_NAME, internalToken);
    this.mockMvc.perform(get(url).header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(DefaultAPIErrorCodes.BAD_REQUEST.getErrorCode()))
        .andExpect(jsonPath("$.message").value(DefaultAPIErrorCodes.BAD_REQUEST.getMessage()));
  }

  @Test
  void testForbiddenException_givenThrowForbiddenException_return403() throws Exception {
    String url = "/testForbiddenException";
    Set<String> permission = Set.of("CAN_ACCESS");
    String internalToken = JwtTokenTestHelper.createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permission);
    internalJwtCacheStore.put(USER_NAME, internalToken);
    this.mockMvc.perform(get(url).header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(DefaultAPIErrorCodes.NOT_AUTHORIZED.getErrorCode()))
        .andExpect(jsonPath("$.message").value(DefaultAPIErrorCodes.NOT_AUTHORIZED.getMessage()));
  }

  @Test
  void testCustomizedException_givenThrowCustomizedException_returnStatusDefinedInCustomizedException() throws Exception {
    String url = "/testCustomizedException";
    Set<String> permission = Set.of("CAN_ACCESS");
    String internalToken = JwtTokenTestHelper.createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permission);
    internalJwtCacheStore.put(USER_NAME, internalToken);
    this.mockMvc.perform(get(url).header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isHttpVersionNotSupported())
        .andExpect(jsonPath("$.code").value(ApiErrorCode.CUSTOMIZED_BAD_REQUEST.getErrorCode()))
        .andExpect(jsonPath("$.message").value("Customized bad request"));
  }

}
