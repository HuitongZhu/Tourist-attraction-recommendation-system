package com.travel.travelweb.repo;

import com.travel.travelweb.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, String> {

    Optional<SysUser> findByUserName(String userName);

    boolean existsByUserName(String userName);

    Optional<SysUser> findByUserNameAndUserType(String userName, String userType);

    Optional<SysUser> findByUserIdAndUserType(String userId, String userType);

    List<SysUser> findByUserType(String userType);

    @Query("SELECT u FROM SysUser u WHERE u.userType = :userType AND (:keyword IS NULL OR :keyword = '' OR u.userName LIKE CONCAT('%', :keyword, '%') OR u.userId LIKE CONCAT('%', :keyword, '%'))")
    List<SysUser> searchUsers(@Param("userType") String userType, @Param("keyword") String keyword);
}
