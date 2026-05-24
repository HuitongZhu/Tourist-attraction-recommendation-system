package com.travel.travelweb.service;

import com.travel.travelweb.entity.Collect;
import com.travel.travelweb.entity.LandCollect;
import com.travel.travelweb.entity.LandLike;
import com.travel.travelweb.entity.Like;
import com.travel.travelweb.repo.CollectRepository;
import com.travel.travelweb.repo.LandCollectRepository;
import com.travel.travelweb.repo.LandLikeRepository;
import com.travel.travelweb.repo.LikeRepository;
import com.travel.travelweb.util.IdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LandInteractionService {

    private final LandLikeRepository landLikeRepository;
    private final LandCollectRepository landCollectRepository;
    private final CollectRepository collectRepository;
    private final LikeRepository likeRepository;

    public LandInteractionService(LandLikeRepository landLikeRepository, LandCollectRepository landCollectRepository, CollectRepository collectRepository, LikeRepository likeRepository) {
        this.landLikeRepository = landLikeRepository;
        this.landCollectRepository = landCollectRepository;
        this.collectRepository = collectRepository;
        this.likeRepository = likeRepository;
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
            likeRepository.deleteById(ex.get().getLikeId());
            return false;
        }
        String likeId = IdGenerator.next("LL");
        LandLike like = new LandLike();
        like.setLikeId(likeId);
        like.setLandscapeId(landscapeId);
        like.setUserId(userId);
        like.setLinkUrl("/landscapes/" + landscapeId);
        like.setLikeTime(LocalDateTime.now());
        landLikeRepository.save(like);
        
        Like l = new Like();
        l.setLikeId(likeId);
        l.setUserId(userId);
        l.setLinkUrl("/landscapes/" + landscapeId);
        l.setLikeTime(LocalDateTime.now());
        likeRepository.save(l);
        return true;
    }

    @Transactional
    public boolean toggleCollect(String landscapeId, String userId) {
        var ex = landCollectRepository.findByLandscapeIdAndUserId(landscapeId, userId);
        if (ex.isPresent()) {
            landCollectRepository.delete(ex.get());
            collectRepository.deleteById(ex.get().getCollectId());
            return false;
        }
        String collectId = IdGenerator.next("LC");
        LandCollect c = new LandCollect();
        c.setCollectId(collectId);
        c.setLandscapeId(landscapeId);
        c.setUserId(userId);
        c.setLinkUrl("/landscapes/" + landscapeId);
        c.setCollectTime(LocalDateTime.now());
        landCollectRepository.save(c);
        
        Collect collect = new Collect();
        collect.setCollectId(collectId);
        collect.setUserId(userId);
        collect.setLinkUrl("/landscapes/" + landscapeId);
        collect.setCollectTime(LocalDateTime.now());
        collectRepository.save(collect);
        return true;
    }
}
