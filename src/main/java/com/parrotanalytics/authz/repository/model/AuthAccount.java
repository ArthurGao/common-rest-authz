package com.arthur.authz.repository.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "account", catalog = "subscription")
@Getter
@Setter
public class AuthAccount {
    @Id
    @Column(name = "idAccount")
    private Integer idAccount;

    @Column(name = "apiId")
    private String apiId;

    @Column(name = "status")
    private Integer status;
}
