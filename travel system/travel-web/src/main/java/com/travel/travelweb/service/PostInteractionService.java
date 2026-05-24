package com.travel.travelweb.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travel.travelweb.entity.Collect;
import com.travel.travelweb.entity.Like;
import com.travel.travelweb.entity.PostCollect;
import com.travel.travelweb.entity.PostLike;
import com.travel.travelweb.repo.CollectRepository;
import com.travel.travelweb.repo.LikeRepository;
import com.travel.travelweb.repo.PostCollectRepository;
import com.travel.travelweb.repo.PostLikeRepository;
import com.travel.travelweb.util.IdGenerator;

@Service
public class PostInteractionService {

    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;
    private final CollectRepository collectRepository;
    private final LikeRepository likeRepository;

    public PostInteractionService(PostLikeRepository postLikeRepository, PostCollectRepository postCollectRepository, CollectRepository collectRepository, LikeRepository likeRepository) {
        this.postLikeRepository = postLikeRepository;
        this.postCollectRepository = postCollectRepository;
        this.collectRepository = collectRepository;
        this.likeRepository = likeRepository;
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
            likeRepository.deleteById(ex.get().getLikeId());
            return false;
        }
        String likeId = IdGenerator.next("PL");
        PostLike like = new PostLike();
        like.setLikeId(likeId);
        like.setRecomId(recomId);
        like.setUserId(userId);
        like.setLinkUrl("/posts/" + recomId);
        like.setLikeTime(LocalDateTime.now());
        postLikeRepository.save(like);
        
        Like l = new Like();
        l.setLikeId(likeId);
        l.setUserId(userId);
        l.setLinkUrl("/posts/" + recomId);
        l.setLikeTime(LocalDateTime.now());
        likeRepository.save(l);
        return true;
    }

    @Transactional
    public boolean toggleCollect(String recomId, String userId) {
        var ex = postCollectRepository.findByRecomIdAndUserId(recomId, userId);
        if (ex.isPresent()) {
            postCollectRepository.delete(ex.get());
            collectRepository.deleteById(ex.get().getCollectId());
            return false;
        }
        String collectId = IdGenerator.next("PC");
        PostCollect c = new PostCollect();
        c.setCollectId(collectId);
        c.setRecomId(recomId);
        c.setUserId(userId);
        c.setLinkUrl("/posts/" + recomId);
        c.setCollectTime(LocalDateTime.now());
        postCollectRepository.save(c);
        
        Collect collect = new Collect();
        collect.setCollectId(collectId);
        collect.setUserId(userId);
        collect.setLinkUrl("/posts/" + recomId);
        collect.setCollectTime(LocalDateTime.now());
        collectRepository.save(collect);
        return true;
    }
}