package com.arthur.authz.repository.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "product_permissions", catalog = "subscription")
@Getter
@Setter
public class AuthProductPermissions {

    @Id
    @Column(name = "permissionId")
    private Integer productPermissionId;

    @Column(name = "productId")
    private Integer productId;

    @Column(name = "permission")
    private String permission;

    @OneToMany(mappedBy = "productPermissions", fetch = FetchType.EAGER)
    private List<AuthUserPermissions> authUserPermissions;

}
