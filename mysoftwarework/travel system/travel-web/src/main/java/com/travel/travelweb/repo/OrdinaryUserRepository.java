package com.travel.travelweb.repo;

import com.travel.travelweb.entity.OrdinaryUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrdinaryUserRepository extends JpaRepository<OrdinaryUser, String> {

    void deleteByUserId(String userId);

    Optional<OrdinaryUser> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);
    
    Optional<OrdinaryUser> findByUserId(String userId);
}
