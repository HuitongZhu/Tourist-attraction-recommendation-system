package com.travel.travelweb.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travel.travelweb.entity.LandCollect;

@Repository
public interface LandCollectRepository extends JpaRepository<LandCollect, String> {

    // 1. 用于 LandInteractionService 第 29 行：判断是否已收藏
    boolean existsByLandscapeIdAndUserId(String landscapeId, String userId);

    // 2. 用于 LandInteractionService 第 51 行：查找特定收藏记录
    Optional<LandCollect> findByLandscapeIdAndUserId(String landscapeId, String userId);

    // 3. 用于 UserService 第 42 行：根据用户 ID 删除所有收藏
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM LandCollect lc WHERE lc.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    // 4. 用于 MyController 第 40 行：获取用户的收藏列表并按时间倒序排列
    List<LandCollect> findByUserIdOrderByCollectTimeDesc(String userId);

    // 5. 用于删除景点时清理关联的收藏记录
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM LandCollect lc WHERE lc.landscapeId = :landscapeId")
    void deleteByLandscapeId(@Param("landscapeId") String landscapeId);

    // 6. 用于统计景点的收藏个数
    long countByLandscapeId(String landscapeId);
}