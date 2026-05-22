package com.travel.travelweb.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travel.travelweb.entity.PostCollect;

@Repository
public interface PostCollectRepository extends JpaRepository<PostCollect, String> {
    boolean existsByRecomIdAndUserId(String recomId, String userId);
    Optional<PostCollect> findByRecomIdAndUserId(String recomId, String userId);
    List<PostCollect> findByUserIdOrderByCollectTimeDesc(String userId);

    void deleteByRecomId(String recomId);

    long countByRecomId(String recomId);
}