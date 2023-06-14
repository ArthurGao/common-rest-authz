package com.arthur.authz.repository.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "user", catalog = "subscription")
@Getter
@Setter
public class AuthUser {

    @Id
    @Column(name = "idUser")
    private Integer idUser;

    @Column(name = "emailAddress")
    private String emailAddress;

    @Column(name = "idAccount")
    private Integer idAccount;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<AuthUserRole> userRoles;

    @OneToMany(mappedBy = "authUser", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<AuthUserPermissions> authUserPermissions;

}
