package com.arthur.authz.annotations;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Can access by TV/SHOW Enterprise users only
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(T(com.arthur.authz.Permission).D360_TV_ENTERPRISE.getValue())")
public @interface AllowD360TVEnterprise {

}
