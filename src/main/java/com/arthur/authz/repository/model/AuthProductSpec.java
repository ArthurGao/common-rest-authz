package com.arthur.authz.repository.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "productspecs", catalog = "subscription")
@Getter
@Setter
public class AuthProductSpec {
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "idAccount")
    private Integer idAccount;
    @Column(name = "idProduct")
    private Integer idProduct;
}
