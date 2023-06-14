package com.arthur.authz.annotations;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Can access by any Enterprise users of D360
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(T(com.arthur.authz.Permission).D360_TV_ENTERPRISE.getValue()) "
    + "or hasAuthority(T(com.arthur.authz.Permission).D360_TALENT_ENTERPRISE.getValue()) "
    + "or hasAuthority(T(com.arthur.authz.Permission).D360_MOVIE_ENTERPRISE.getValue())")
public @interface AllowAnyD360Enterprise {

}
