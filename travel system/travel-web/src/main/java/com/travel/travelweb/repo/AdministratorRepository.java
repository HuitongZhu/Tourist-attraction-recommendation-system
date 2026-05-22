package com.travel.travelweb.repo;

import com.travel.travelweb.entity.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdministratorRepository extends JpaRepository<Administrator, String> {

    Optional<Administrator> findByPhoneNumber(String phoneNumber);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Administrator a WHERE a.phoneNumber = :phoneNumber")
    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}