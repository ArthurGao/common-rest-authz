package com.arthur.authz.repository.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "user_permissions", catalog = "subscription")
@Getter
@Setter
public class AuthUserPermissions {

    @Id
    @Column(name = "userPermissionId")
    private Integer userPermissionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userId")
    @JsonBackReference
    private AuthUser authUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "permissionId")
    @JsonBackReference
    private AuthProductPermissions productPermissions;


}
