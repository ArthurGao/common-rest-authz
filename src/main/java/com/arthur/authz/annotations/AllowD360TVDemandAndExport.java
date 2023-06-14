package com.arthur.authz.annotations;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Can access by Third-party has subscription on TV/SHOW API and Custom Data Export
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(T(com.arthur.authz.Permission).D360_TV_DEMAND_AND_EXPORT.getValue())")
public @interface AllowD360TVDemandAndExport {

}
