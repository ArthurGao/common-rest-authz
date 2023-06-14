package com.arthur.authz.annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Can access by Talent Lite users only
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(T(com.arthur.authz.Permission).D360_TALENT_LITE.getValue())")
public @interface AllowD360TalentLite {

}