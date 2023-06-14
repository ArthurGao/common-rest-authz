package com.arthur.authz;

import com.arthur.AuthzTestBase;
import com.arthur.authz.container.MySQLExtension;
import com.arthur.authz.repository.AuthProductSpecRepository;
import com.arthur.authz.repository.model.AuthProductSpec;
import com.arthur.rest.exceptions.DefaultAPIErrorCodes;
import nl.altindag.log.LogCaptor;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static com.arthur.authz.helper.JwtTokenTestHelper.createExternalToken;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({MySQLExtension.class, SpringExtension.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class StatelessAuthenticationFilterWithoutRedisIT extends AuthzTestBase {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private AuthProductSpecRepository authProductSpecRepository;

  @BeforeAll
  public static void tearDownDocker() {

  }

  @BeforeEach
  public void setup() {
    authProductSpecRepository.deleteAll();
  }

  @Test
  void givenRequestWithoutAuth_thenReturnStatusCode401() throws Exception {
    mockMvc.perform(get("/apiWithoutSecuredAnnotation"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(DefaultAPIErrorCodes.AUTHENTICATION_FAILED.getErrorCode()))
        .andExpect(jsonPath("$.message").value(DefaultAPIErrorCodes.AUTHENTICATION_FAILED.getMessage()));
  }

  @Test
  void givenRequestWithTokenExpired_thenReturnStatusCode401() throws Exception {
    String externalToken = createExternalToken(USER_NAME, EXPIRY_ELAPSED_TIME_IN_SECONDS);

    mockMvc.perform(get("/apiWithSecuredAnnotation")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, externalToken))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(DefaultAPIErrorCodes.AUTHENTICATION_FAILED.getErrorCode()))
        .andExpect(jsonPath("$.message").value(DefaultAPIErrorCodes.AUTHENTICATION_FAILED.getMessage()));
  }

  @Test
  void givenValidExternalToken_whenInternalTokenCannotBeCreated_UserDoesNotExist_thenReturnStatusCode401() throws Exception {
    LogCaptor errorLogs = LogCaptor.forClass(StatelessAuthenticationFilter.class);

    EXTERNAL_TOKEN = createExternalToken(USER_NAME_DOES_NOT_EXIST);
    mockMvc.perform(get("/apiWithSecuredAnnotation")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(DefaultAPIErrorCodes.AUTHENTICATION_FAILED.getErrorCode()))
        .andExpect(jsonPath("$.message").value(DefaultAPIErrorCodes.AUTHENTICATION_FAILED.getMessage()));

    assertThat(errorLogs.getErrorLogs()).hasSize(1);
    assertThat(errorLogs.getErrorLogs())
        .contains(format("AuthenticationFilter failed, caused by: Found no internal token for userEmailAddress: %s", USER_NAME_DOES_NOT_EXIST));
  }

  @Test
  void givenValidExternalAndInternalToken_whenInvokeSecuredAPI_andPermissionNotMatch_thenReturnStatusCode403() throws Exception {
    mockMvc.perform(get("/apiWithSecuredAnnotation")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(DefaultAPIErrorCodes.NOT_AUTHORIZED.getErrorCode()))
        .andExpect(jsonPath("$.message").value(DefaultAPIErrorCodes.NOT_AUTHORIZED.getMessage()));
  }

  @Test
  void givenValidTokens_whenInvokeSecuredAPI_withoutRequiredPermission_thenReturnStatusCode403() throws Exception {
    mockMvc.perform(get("/allowAnyD360Enterprise")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(DefaultAPIErrorCodes.NOT_AUTHORIZED.getErrorCode()))
        .andExpect(jsonPath("$.message").value(DefaultAPIErrorCodes.NOT_AUTHORIZED.getMessage()));
  }

  @ParameterizedTest
  @EnumSource(value = Permission.class, names = {"D360_MOVIE_ENTERPRISE", "D360_TV_ENTERPRISE", "D360_TALENT_ENTERPRISE"})
  void givenValidTokens_whenInvokeSecuredAPI_withAllowAnyD360Enterprise_thenReturnStatusOk(Permission permission) throws Exception {

    EXTERNAL_TOKEN = createExternalToken(USER_NAME);

    if (permission == Permission.D360_TALENT_ENTERPRISE) {
      saveProductSpec(7);
    }
    if (permission == Permission.D360_TV_ENTERPRISE) {
      saveProductSpec(1);
    }
    if (permission == Permission.D360_MOVIE_ENTERPRISE) {
      saveProductSpec(9);
    }

    mockMvc.perform(get("/allowAnyD360Enterprise")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isOk());
  }

  private AuthProductSpec getProductSpec(int idProduct) {
    AuthProductSpec productSpec = new AuthProductSpec();
    productSpec.setId(RandomUtils.nextInt());
    productSpec.setIdAccount(100);
    productSpec.setIdProduct(idProduct);
    return productSpec;
  }

  @Test
  void givenValidTokens_whenInvokeAllowD360MovieEnterpriseAPI_andMeetRequiredPermission_thenReturn200() throws Exception {
    saveProductSpec(9);

    mockMvc.perform(get("/allowD360MovieEnterprise")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isOk());
  }

  private void saveProductSpec(int idProduct) {
    authProductSpecRepository.save(getProductSpec(idProduct));
  }

  @Test
  void givenValidTokens_whenInvokeAllowD360MovieEnterpriseAPI_andDoesNotMeetRequiredPermission_thenReturn403() throws Exception {

    saveProductSpec(1);

    mockMvc.perform(get("/allowD360MovieEnterprise")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isForbidden());
  }

  @Test
  void givenValidTokens_whenInvokeAllowD360TalentEnterpriseAPI_andMeetRequiredPermission_thenReturn200() throws Exception {
    saveProductSpec(7);

    mockMvc.perform(get("/allowD360TalentEnterprise")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isOk());
  }

  @Test
  void givenValidTokens_whenInvokeAllowD360TVEnterpriseAPI_andMeetRequiredPermission_thenReturn200() throws Exception {

    saveProductSpec(1);

    mockMvc.perform(get("/allowD360TVEnterprise")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isOk());
  }

  @Test
  void givenValidTokens_whenInvokeTalentLiteAPI_andMeetRequiredPermission_thenReturn200() throws Exception {

    saveProductSpec(8);

    mockMvc.perform(get("/allowTalentLite")
                    .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
            .andExpect(status().isOk());
  }

  @Test
  void givenValidTokens_whenTalentEnterpriseAPI_andMeetRequiredPermission_thenReturn200() throws Exception {

    saveProductSpec(8);

    mockMvc.perform(get("/allowD360TalentEnterprise")
                    .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
            .andExpect(status().isForbidden());
  }

  @Test
  void givenValidTokens_whenInvokeAPIWithMultipleEnterpriseAnnotations_andPartialMeetRequiredPermission_thenReturn200() throws Exception {

    saveProductSpec(9);

    mockMvc.perform(get("/allowMultipleD360EnterpriseAnnotations")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isOk());
  }
}