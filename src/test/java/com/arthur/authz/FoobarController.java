package com.arthur.authz;

import com.arthur.authz.annotations.*;
import com.arthur.authz.models.UserAuthentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class FoobarController {

  @GetMapping("apiWithoutSecuredAnnotation")
  public ResponseEntity<UserAuthentication> apiWithoutSecuredAnnotation() {

    return ResponseEntity.ok((UserAuthentication) SecurityContextHolder.getContext().getAuthentication());
  }

  @GetMapping("apiWithSecuredAnnotation")
  @PreAuthorize("hasAuthority('CAN_ACCESS')")
  public ResponseEntity<UserAuthentication> apiWithSecuredAnnotation() {
    return ResponseEntity.ok((UserAuthentication) SecurityContextHolder.getContext().getAuthentication());
  }

  @GetMapping("allowAnyD360Enterprise")
  @AllowAnyD360Enterprise
  public ResponseEntity<UserAuthentication> allowAnyD360Enterprise() {
    return ResponseEntity.ok((UserAuthentication) SecurityContextHolder.getContext().getAuthentication());
  }

  @GetMapping("allowD360MovieEnterprise")
  @AllowD360MovieEnterprise
  public ResponseEntity<UserAuthentication> allowD360MovieEnterprise() {
    return ResponseEntity.ok((UserAuthentication) SecurityContextHolder.getContext().getAuthentication());
  }

  @GetMapping("allowD360TVEnterprise")
  @AllowD360TVEnterprise
  public ResponseEntity<UserAuthentication> allowD360TVEnterprise() {
    return ResponseEntity.ok((UserAuthentication) SecurityContextHolder.getContext().getAuthentication());
  }

  @GetMapping("allowD360TalentEnterprise")
  @AllowD360TalentEnterprise
  public ResponseEntity<UserAuthentication> allowD360TalentEnterprise() {
    return ResponseEntity.ok((UserAuthentication) SecurityContextHolder.getContext().getAuthentication());
  }

  @GetMapping("allowMultipleD360EnterpriseAnnotations")
  @AllowD360MovieEnterprise
  @AllowD360TVEnterprise
  @AllowD360TalentEnterprise
  public ResponseEntity<UserAuthentication> allowMultipleD360EnterpriseAnnotations() {
    return ResponseEntity.ok((UserAuthentication) SecurityContextHolder.getContext().getAuthentication());
  }

  @GetMapping("allowD360MovieDemandAndExport")
  @AllowD360MovieDemandAndExport
  public ResponseEntity<UserAuthentication> allowD360MovieDemandAndExport() {
    return ResponseEntity.ok((UserAuthentication) SecurityContextHolder.getContext().getAuthentication());
  }

  @GetMapping("allowD360TalentDemandAndExport")
  @AllowD360TalentDemandAndExport
  public ResponseEntity<UserAuthentication> allowD360TalentDemandAndExport() {
    return ResponseEntity.ok((UserAuthentication) SecurityContextHolder.getContext().getAuthentication());
  }

  @GetMapping("allowD360TVDemandAndExport")
  @AllowD360TVDemandAndExport
  public ResponseEntity<UserAuthentication> allowD360TVDemandAndExport() {
    return ResponseEntity.ok((UserAuthentication) SecurityContextHolder.getContext().getAuthentication());
  }

  @GetMapping("allowTalentLite")
  @AllowD360TalentLite
  public ResponseEntity<UserAuthentication> allowTalentLite() {
    return ResponseEntity.ok((UserAuthentication) SecurityContextHolder.getContext().getAuthentication());
  }

  @GetMapping("allowMovieLite")
  @AllowD360MovieLite
  public ResponseEntity<UserAuthentication> allowMovieLite() {
    return ResponseEntity.ok((UserAuthentication) SecurityContextHolder.getContext().getAuthentication());
  }

  @GetMapping("allowTVLite")
  @AllowD360TVLite
  public ResponseEntity<UserAuthentication> allowTVLite() {
    return ResponseEntity.ok((UserAuthentication) SecurityContextHolder.getContext().getAuthentication());
  }

  @GetMapping("allowAnyD360User")
  @AllowAnyD360User
  public ResponseEntity<UserAuthentication> allowAnyD360User() {
    return ResponseEntity.ok((UserAuthentication) SecurityContextHolder.getContext().getAuthentication());
  }
}