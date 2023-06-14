package com.arthur.authz;

import static com.arthur.authz.helper.JwtTokenTestHelper.createExternalToken;
import static com.arthur.authz.helper.JwtTokenTestHelper.createInternalJwt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arthur.AuthzTestBase;
import com.arthur.authz.container.MySQLExtension;
import com.arthur.authz.container.RedisExtension;
import com.arthur.authz.helper.JwtTokenTestHelper;
import com.arthur.rest.exceptions.DefaultAPIErrorCodes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith({RedisExtension.class, MySQLExtension.class, SpringExtension.class})
class StatelessAuthenticationFilterWithRedisIT extends AuthzTestBase {

  @Autowired
  private MockMvc mockMvc;

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
  void givenValidExternalToken_whenInternalTokenNotExistButCanBeGenerated_thenReturnStatusCode401() throws Exception {
    mockMvc.perform(get("/apiWithSecuredAnnotation")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(DefaultAPIErrorCodes.NOT_AUTHORIZED.getErrorCode()))
        .andExpect(jsonPath("$.message").value(DefaultAPIErrorCodes.NOT_AUTHORIZED.getMessage()));
  }

  @Test
  void givenValidExternalToken_whenInternalTokenNotExistButCannotBeGenerated_thenReturnStatusCode401() throws Exception {
    mockMvc.perform(get("/apiWithSecuredAnnotation")
                    .header(StatelessAuthenticationFilter.AUTH_TOKEN, JwtTokenTestHelper.createExternalToken(USER_NAME_DOES_NOT_EXIST)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(DefaultAPIErrorCodes.AUTHENTICATION_FAILED.getErrorCode()))
            .andExpect(jsonPath("$.message").value(DefaultAPIErrorCodes.AUTHENTICATION_FAILED.getMessage()));
  }

  @Test
  void givenValidExternalAndInternalToken_whenInvokeSecuredAPI_andPermissionNotMatch_thenReturnStatusCode403() throws Exception {

    Set<String> permission = Set.of("SomethingElse");
    String internalToken = createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permission);
    internalJwtCacheStore.put(USER_NAME, internalToken);

    mockMvc.perform(get("/apiWithSecuredAnnotation")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(DefaultAPIErrorCodes.NOT_AUTHORIZED.getErrorCode()))
        .andExpect(jsonPath("$.message").value(DefaultAPIErrorCodes.NOT_AUTHORIZED.getMessage()));
  }

  @Test
  void givenValidExternalAndInternalToken_whenInvokeSecuredAPI_thenReturnStatusOk() throws Exception {

    Set<String> permission = Set.of("CAN_ACCESS");
    String internalToken = createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permission);
    internalJwtCacheStore.put(USER_NAME, internalToken);

    mockMvc.perform(get("/apiWithSecuredAnnotation")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isOk());

  }

  @Test
  void givenValidApiKey_whenInvokeAPIWithPermission_thenReturnStatusOk() throws Exception {
    mockMvc.perform(get("/allowD360MovieEnterprise")
                    .header(StatelessAuthenticationFilter.EXTERNAL_API_KEY, "52252321-0f2a-4a5b-891a-cc47f8d2f39f"))
            .andExpect(status().isOk());
  }

  @Test
  void givenValidApiKey_whenInvokeAPIWithoutPermission_thenReturnStatusForbidden() throws Exception {
    mockMvc.perform(get("/allowD360TVEnterprise")
                    .header(StatelessAuthenticationFilter.EXTERNAL_API_KEY, "52252321-0f2a-4a5b-891a-cc47f8d2f39f"))
            .andExpect(status().isForbidden());
  }

  @Test
  void givenValidApiKey_whenInvokeAPIWithInvalidApiKey_thenReturnStatusUnauthorized() throws Exception {
    mockMvc.perform(get("/allowD360TVEnterprise")
                    .header(StatelessAuthenticationFilter.EXTERNAL_API_KEY, UUID.randomUUID().toString()))
            .andExpect(status().isUnauthorized());
  }

  @Test
  void givenValidTokens_whenInvokeSecuredAPI_withoutRequiredPermission_thenReturnStatusCode403() throws Exception {

    Set<String> permissions = Set.of("SomethingElse");
    String internalToken = createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permissions);
    internalJwtCacheStore.put(USER_NAME, internalToken);

    mockMvc.perform(get("/allowAnyD360Enterprise")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(DefaultAPIErrorCodes.NOT_AUTHORIZED.getErrorCode()))
        .andExpect(jsonPath("$.message").value(DefaultAPIErrorCodes.NOT_AUTHORIZED.getMessage()));
  }

  @ParameterizedTest
  @EnumSource(value = Permission.class, names = {"D360_MOVIE_ENTERPRISE", "D360_TV_ENTERPRISE", "D360_TALENT_ENTERPRISE"})
  void givenValidTokens_whenInvokeSecuredAPI_withAllowAnyD360Enterprise_thenReturnStatusOk(Permission permission) throws Exception {

    Set<String> permissions = Set.of(permission.getValue());
    String internalToken = createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permissions);
    internalJwtCacheStore.put(USER_NAME, internalToken);

    mockMvc.perform(get("/allowAnyD360Enterprise")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isOk());
  }

  @Test
  void givenValidTokens_whenInvokeAllowD360MovieEnterpriseAPI_andMeetRequiredPermission_thenReturn200() throws Exception {

    Set<String> permissions = Set.of(Permission.D360_MOVIE_ENTERPRISE.getValue());
    String internalToken = createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permissions);
    internalJwtCacheStore.put(USER_NAME, internalToken);

    mockMvc.perform(get("/allowD360MovieEnterprise")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(strings = {"/allowD360MovieEnterprise", "/allowD360TalentEnterprise", "/allowD360TVEnterprise", "/allowMultipleD360EnterpriseAnnotations"})
  void givenValidTokens_whenInvokeAllowD360MovieEnterpriseAPI_andDoesNotMeetRequiredPermission_thenReturn403(String path) throws Exception {

    Set<String> permissions = Set.of("SOMETHING_ELSE");
    String internalToken = createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permissions);
    internalJwtCacheStore.put(USER_NAME, internalToken);

    mockMvc.perform(get(path)
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isForbidden());
  }

  @Test
  void givenValidTokens_whenInvokeAllowD360TalentEnterpriseAPI_andMeetRequiredPermission_thenReturn200() throws Exception {

    Set<String> permissions = Set.of(Permission.D360_TALENT_ENTERPRISE.getValue());
    String internalToken = createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permissions);
    internalJwtCacheStore.put(USER_NAME, internalToken);

    mockMvc.perform(get("/allowD360TalentEnterprise")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isOk());
  }

  @Test
  void givenValidTokens_whenInvokeAllowD360TVEnterpriseAPI_andMeetRequiredPermission_thenReturn200() throws Exception {

    Set<String> permissions = Set.of(Permission.D360_TV_ENTERPRISE.getValue());
    String internalToken = createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permissions);
    internalJwtCacheStore.put(USER_NAME, internalToken);

    mockMvc.perform(get("/allowD360TVEnterprise")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isOk());
  }

  @Test
  void givenValidTokens_whenInvokeAllowD360TVEnterpriseAPI_andMeetRequiredPermission_andRedisCacheStoreEmpty_thenReturn200() throws Exception {

    assertThat(internalJwtCacheStore).isEmpty();

    mockMvc.perform(get("/allowD360TVEnterprise")
                    .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
            .andExpect(status().isOk());

    assertThat(internalJwtCacheStore).isNotEmpty();
    assertThat(internalJwtCacheStore.get(USER_NAME)).isNotEmpty();
  }

  @Test
  void givenValidTokens_whenInvokeTalentLite_andMeetRequiredPermission_thenReturn200() throws Exception {

    Set<String> permissions = Set.of(Permission.D360_TALENT_LITE.getValue());
    String internalToken = createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permissions);
    internalJwtCacheStore.put(USER_NAME, internalToken);

    mockMvc.perform(get("/allowTalentLite")
                    .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
            .andExpect(status().isOk());
  }

  @Test
  void givenValidTokens_whenInvokeAPIWithMultipleEnterpriseAnnotations_andPartialMeetRequiredPermission_thenReturn200() throws Exception {

    Set<String> permissions = Set.of(Permission.D360_MOVIE_ENTERPRISE.getValue());
    String internalToken = createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permissions);
    internalJwtCacheStore.put(USER_NAME, internalToken);

    mockMvc.perform(get("/allowMultipleD360EnterpriseAnnotations")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isOk());
  }

  @ParameterizedTest
  @MethodSource("generateValidEnterprisePermissions")
  void givenValidTokens_whenInvokeAPIWithMultipleEnterpriseAnnotations_andMeetRequiredPermission_thenReturn200(Set<String> permissions) throws Exception {

    String internalToken = createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permissions);
    internalJwtCacheStore.put(USER_NAME, internalToken);

    mockMvc.perform(get("/allowMultipleD360EnterpriseAnnotations")
        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isOk());
  }

  @ParameterizedTest
  @CsvSource({
      "D360:Movie:DemandDataAPIAndDataExport,/allowD360MovieDemandAndExport",
      "D360:Talent:DemandDataAPIAndDataExport,/allowD360TalentDemandAndExport",
      "D360:TV:DemandDataAPIAndDataExport,/allowD360TVDemandAndExport"})
  void givenValidTokens_whenInvokeAllowD360DemandAndDataExportAPI_andMeetRequiredPermission_thenReturn200(String permission, String url) throws Exception {

    Set<String> permissions = Set.of(permission);
    String internalToken = createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permissions);
    internalJwtCacheStore.put(USER_NAME, internalToken);

    mockMvc.perform(get(url)
            .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(strings = {"/allowD360MovieDemandAndExport", "/allowD360TalentDemandAndExport", "/allowD360TVDemandAndExport"})
  void givenValidTokens_whenInvokeAllowD360DemandAndDataExportAPI_andDoesNotMeetRequiredPermission_thenReturn403(String path) throws Exception {

    Set<String> permissions = Set.of("SOMETHING_ELSE");
    String internalToken = createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permissions);
    internalJwtCacheStore.put(USER_NAME, internalToken);

    mockMvc.perform(get(path)
            .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @CsvSource({
          "/allowD360MovieDemandAndExport",
          "/allowD360TalentDemandAndExport",
          "/allowD360TVDemandAndExport"})
  void givenValidTokens_whenInvokeAllowD360DemandAndDataExportAPI_andFromExternalAPIWithAuthorisation_andMeetRequiredPermission_thenReturn200(String url) throws Exception {
    String authorization = "Bearer " + JwtTokenTestHelper.createExternalAuthorisation(API_USER_NAME, Instant.now().plus(60, ChronoUnit.MINUTES).toEpochMilli());
    mockMvc.perform(get(url)
                    .header(StatelessAuthenticationFilter.AUTHORISATION, authorization))
            .andExpect(status().isOk());
  }

  @ParameterizedTest
  @CsvSource({
          "/allowD360MovieDemandAndExport",
          "/allowD360TalentDemandAndExport",
          "/allowD360TVDemandAndExport"})
  void givenValidTokens_whenInvokeAllowD360DemandAndDataExportAPI_andFromExternalAPIWithAuthorisation_andDoesNotMeetRequiredPermission_thenReturn403(String url) throws Exception {
    String authorization = "Bearer " + JwtTokenTestHelper.createExternalAuthorisation(USER_NAME, Instant.now().plus(60, ChronoUnit.MINUTES).toEpochMilli());
    mockMvc.perform(get(url)
                    .header(StatelessAuthenticationFilter.AUTHORISATION, authorization))
            .andExpect(status().isForbidden());
  }

  @Test
  void givenValidTokens_whenInvokeAllowD360DemandAndDataExportAPI_andFromExternalAPIWithAuthorisation_andMeetsRequiredPermission_thenReturn200() throws Exception {
    String authorization = "Bearer " + JwtTokenTestHelper.createExternalAuthorisation(API_USER_NAME, Instant.now().plus(60, ChronoUnit.MINUTES).toEpochMilli());
    mockMvc.perform(get("/allowD360TalentDemandAndExport")
                    .header(StatelessAuthenticationFilter.AUTHORISATION, authorization))
            .andExpect(status().isOk());
  }

  @Test
  void givenExpiredTokens_whenInvokeAllowD360DemandAndDataExportAPI_thenReturn403() throws Exception {
    String authorization = "Bearer " + JwtTokenTestHelper.createExternalAuthorisation(USER_NAME, Instant.now().minus(60, ChronoUnit.MINUTES).toEpochMilli());
    mockMvc.perform(get("/allowD360TalentDemandAndExport")
                    .header(StatelessAuthenticationFilter.AUTHORISATION, authorization))
            .andExpect(status().isUnauthorized());
  }

  @Test
  void givenInvalidTokens_whenInvokeAllowD360DemandAndDataExportAPI_thenReturn403() throws Exception {
    String authorization = JwtTokenTestHelper.createExternalAuthorisation(USER_NAME, Instant.now().plus(60, ChronoUnit.MINUTES).toEpochMilli());
    mockMvc.perform(get("/allowD360TalentDemandAndExport")
                    .header(StatelessAuthenticationFilter.AUTHORISATION, authorization))
            .andExpect(status().isUnauthorized());
  }

  @Test
  void givenValidTokens_whenInvokeAllowMovieLite_andMeetRequiredPermission_thenReturn200() throws Exception {

    Set<String> permissions = Set.of(Permission.D360_MOVIE_LITE.getValue());
    String internalToken = createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permissions);
    internalJwtCacheStore.put(USER_NAME, internalToken);

    mockMvc.perform(get("/allowMovieLite")
                        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
           .andExpect(status().isOk());
  }

  @Test
  void givenValidTokens_whenInvokeAllowTVLite_andMeetRequiredPermission_thenReturn200() throws Exception {

    Set<String> permissions = Set.of(Permission.D360_TV_LITE.getValue());
    String internalToken = createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permissions);
    internalJwtCacheStore.put(USER_NAME, internalToken);

    mockMvc.perform(get("/allowTVLite")
                        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
           .andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(strings = {"D360:TV:Enterprise", "D360:TV:Lite", "D360:Talent:Enterprise", "D360:Talent:Lite", "D360:Movie:Enterprise", "D360:Movie:Lite"})
  void givenValidTokens_whenInvokeAllowAnyD360User_andMeetRequiredPermission_thenReturn200(String permission) throws Exception {


    Set<String> permissions = Set.of(permission);
    String internalToken = createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permissions);
    internalJwtCacheStore.put(USER_NAME, internalToken);

    mockMvc.perform(get("/allowAnyD360User")
                        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
           .andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(strings = {"Something"})
  void givenValidTokens_whenInvokeAllowAnyD360User_andDoNotMeetRequiredPermission_thenReturn403(String permission) throws Exception {


    Set<String> permissions = Set.of(permission);
    String internalToken = createInternalJwt(USER_NAME, USER_ID, ACCOUNT_ID, permissions);
    internalJwtCacheStore.put(USER_NAME, internalToken);

    mockMvc.perform(get("/allowAnyD360User")
                        .header(StatelessAuthenticationFilter.AUTH_TOKEN, EXTERNAL_TOKEN))
           .andExpect(status().isForbidden());
  }


  private static Stream<Arguments> generateValidEnterprisePermissions() {
    return Stream.of(
        Arguments.of(
            Set.of(
                Permission.D360_TV_ENTERPRISE.getValue(),
                Permission.D360_TALENT_ENTERPRISE.getValue(),
                Permission.D360_MOVIE_ENTERPRISE.getValue()
            )
        )
    );
  }
}