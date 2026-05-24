package com.travel.travelweb.repo;

import com.travel.travelweb.entity.OrdinaryUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrdinaryUserRepository extends JpaRepository<OrdinaryUser, String> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM OrdinaryUser ou WHERE ou.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    Optional<OrdinaryUser> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);
    
    Optional<OrdinaryUser> findByUserId(String userId);
}
