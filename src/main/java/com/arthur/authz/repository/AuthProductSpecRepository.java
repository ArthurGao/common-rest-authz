package com.arthur.authz.repository;

import com.arthur.authz.repository.model.AuthProductSpec;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthProductSpecRepository extends CrudRepository<AuthProductSpec, Integer> {
    @Query("SELECT p.idProduct FROM AuthProductSpec p WHERE p.idAccount = :accountId")
    List<Integer> getProductIdsByIdAccount(@Param("accountId") int accountId);
}
