package com.arthur.authz.repository;

import com.arthur.authz.repository.model.AuthAccount;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthAccountRepository extends CrudRepository<AuthAccount, Integer> {

    @Query("SELECT a FROM AuthAccount a WHERE a.apiId = :apiId and a.status = 1")
    AuthAccount findAccountByApiId(@Param("apiId") String apiId);
}
