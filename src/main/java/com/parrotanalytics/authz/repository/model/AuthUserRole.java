package com.arthur.authz.repository.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "userroles", catalog = "subscription")
@Getter
@Setter
public class AuthUserRole {

    @Id
    @Column(name = "idUserRoles")
    private Integer idUserRoles;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idUser")
    @JsonBackReference
    private AuthUser user;
    @Column(name = "idRole")
    private Integer idRole;
}
