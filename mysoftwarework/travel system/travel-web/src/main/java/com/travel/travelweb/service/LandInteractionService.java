package com.travel.travelweb.service;

import com.travel.travelweb.entity.LandCollect;
import com.travel.travelweb.entity.LandLike;
import com.travel.travelweb.repo.LandCollectRepository;
import com.travel.travelweb.repo.LandLikeRepository;
import com.travel.travelweb.util.IdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LandInteractionService {

    private final LandLikeRepository landLikeRepository;
    private final LandCollectRepository landCollectRepository;

    public LandInteractionService(LandLikeRepository landLikeRepository, LandCollectRepository landCollectRepository) {
        this.landLikeRepository = landLikeRepository;
        this.landCollectRepository = landCollectRepository;
    }

    public boolean liked(String landscapeId, String userId) {
        return landLikeRepository.existsByLandscapeIdAndUserId(landscapeId, userId);
    }

    public boolean collected(String landscapeId, String userId) {
        return landCollectRepository.existsByLandscapeIdAndUserId(landscapeId, userId);
    }

    @Transactional
    public boolean toggleLike(String landscapeId, String userId) {
        var ex = landLikeRepository.findByLandscapeIdAndUserId(landscapeId, userId);
        if (ex.isPresent()) {
            landLikeRepository.delete(ex.get());
            return false;
        }
        LandLike like = new LandLike();
        like.setLikeId(IdGenerator.next("LL"));
        like.setLandscapeId(landscapeId);
        like.setUserId(userId);
        like.setLinkUrl("/landscapes/" + landscapeId);
        like.setLikeTime(LocalDateTime.now());
        landLikeRepository.save(like);
        return true;
    }

    @Transactional
    public boolean toggleCollect(String landscapeId, String userId) {
        var ex = landCollectRepository.findByLandscapeIdAndUserId(landscapeId, userId);
        if (ex.isPresent()) {
            landCollectRepository.delete(ex.get());
            return false;
        }
        LandCollect c = new LandCollect();
        c.setCollectId(IdGenerator.next("LC"));
        c.setLandscapeId(landscapeId);
        c.setUserId(userId);
        c.setLinkUrl("/landscapes/" + landscapeId);
        c.setCollectTime(LocalDateTime.now());
        landCollectRepository.save(c);
        return true;
    }
}
