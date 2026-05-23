package com.travel.travelweb.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travel.travelweb.entity.PostCollect;
import com.travel.travelweb.entity.PostLike;
import com.travel.travelweb.repo.PostCollectRepository;
import com.travel.travelweb.repo.PostLikeRepository;
import com.travel.travelweb.util.IdGenerator;

@Service
public class PostInteractionService {

    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;

    public PostInteractionService(PostLikeRepository postLikeRepository, PostCollectRepository postCollectRepository) {
        this.postLikeRepository = postLikeRepository;
        this.postCollectRepository = postCollectRepository;
    }

    public boolean liked(String recomId, String userId) {
        return postLikeRepository.existsByRecomIdAndUserId(recomId, userId);
    }

    public boolean collected(String recomId, String userId) {
        return postCollectRepository.existsByRecomIdAndUserId(recomId, userId);
    }

    public long likeCount(String recomId) {
        return postLikeRepository.countByRecomId(recomId);
    }

    public long collectCount(String recomId) {
        return postCollectRepository.countByRecomId(recomId);
    }

    @Transactional
    public boolean toggleLike(String recomId, String userId) {
        var ex = postLikeRepository.findByRecomIdAndUserId(recomId, userId);
        if (ex.isPresent()) {
            postLikeRepository.delete(ex.get());
            return false;
        }
        PostLike like = new PostLike();
        like.setLikeId(IdGenerator.next("PL"));
        like.setRecomId(recomId);
        like.setUserId(userId);
        like.setLinkUrl("/posts/" + recomId);
        like.setLikeTime(LocalDateTime.now());
        postLikeRepository.save(like);
        return true;
    }

    @Transactional
    public boolean toggleCollect(String recomId, String userId) {
        var ex = postCollectRepository.findByRecomIdAndUserId(recomId, userId);
        if (ex.isPresent()) {
            postCollectRepository.delete(ex.get());
            return false;
        }
        PostCollect c = new PostCollect();
        c.setCollectId(IdGenerator.next("PC"));
        c.setRecomId(recomId);
        c.setUserId(userId);
        c.setLinkUrl("/posts/" + recomId);
        c.setCollectTime(LocalDateTime.now());
        postCollectRepository.save(c);
        return true;
    }
}