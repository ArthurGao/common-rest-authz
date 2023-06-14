package com.arthur.authz.repository;

import com.arthur.authz.repository.model.AuthUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthUserRepository extends CrudRepository<AuthUser, Integer> {

    @Query("SELECT u FROM AuthUser u WHERE u.emailAddress = :emailAddress")
    AuthUser findUserByEmailAddress(@Param("emailAddress") String emailAddress);

    @Query("SELECT u FROM AuthUser u WHERE u.idAccount = :idAccount and u.emailAddress like '%apiuser%'")
    AuthUser findUserByAccountId(@Param("idAccount") Integer idAccount);
}
