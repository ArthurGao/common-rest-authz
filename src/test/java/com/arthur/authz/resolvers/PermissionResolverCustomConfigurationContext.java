package com.arthur.authz.resolvers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
    PermissionResolver.class,
    AudienceInsightsPermissionResolver.class,
    DemandAPIPermissionResolver.class,
    EnterpriseUserPermissionResolver.class,
    LiteUserPermissionResolver.class}
)
public class PermissionResolverCustomConfigurationContext {

}
