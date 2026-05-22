package com.travel.travelweb.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travel.travelweb.entity.PostLike;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, String> {
    boolean existsByRecomIdAndUserId(String recomId, String userId);
    Optional<PostLike> findByRecomIdAndUserId(String recomId, String userId);
    long countByRecomId(String recomId);
    List<PostLike> findByUserIdOrderByLikeTimeDesc(String userId);

    void deleteByRecomId(String recomId);
}