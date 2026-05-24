package com.travel.travelweb.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travel.travelweb.entity.Collect;
import com.travel.travelweb.entity.LandCollect;
import com.travel.travelweb.entity.PostCollect;
import com.travel.travelweb.repo.CollectRepository;
import com.travel.travelweb.repo.LandCollectRepository;
import com.travel.travelweb.repo.PostCollectRepository;

@Service
public class CollectMigrationService {

    private final PostCollectRepository postCollectRepository;
    private final LandCollectRepository landCollectRepository;
    private final CollectRepository collectRepository;

    public CollectMigrationService(PostCollectRepository postCollectRepository,
                                   LandCollectRepository landCollectRepository,
                                   CollectRepository collectRepository) {
        this.postCollectRepository = postCollectRepository;
        this.landCollectRepository = landCollectRepository;
        this.collectRepository = collectRepository;
    }

    @Transactional
    public int migrateAllCollects() {
        List<Collect> collectList = new ArrayList<>();

        List<PostCollect> postCollects = postCollectRepository.findAll();
        for (PostCollect pc : postCollects) {
            Collect collect = new Collect();
            collect.setCollectId(pc.getCollectId());
            collect.setLinkUrl(pc.getLinkUrl());
            collect.setUserId(pc.getUserId());
            collect.setCollectTime(pc.getCollectTime());
            collectList.add(collect);
        }

        List<LandCollect> landCollects = landCollectRepository.findAll();
        for (LandCollect lc : landCollects) {
            Collect collect = new Collect();
            collect.setCollectId(lc.getCollectId());
            collect.setLinkUrl(lc.getLinkUrl());
            collect.setUserId(lc.getUserId());
            collect.setCollectTime(lc.getCollectTime());
            collectList.add(collect);
        }

        collectRepository.saveAll(collectList);
        return collectList.size();
    }

    @Transactional
    public int migratePostCollects() {
        List<Collect> collectList = new ArrayList<>();
        List<PostCollect> postCollects = postCollectRepository.findAll();
        
        for (PostCollect pc : postCollects) {
            Collect collect = new Collect();
            collect.setCollectId(pc.getCollectId());
            collect.setLinkUrl(pc.getLinkUrl());
            collect.setUserId(pc.getUserId());
            collect.setCollectTime(pc.getCollectTime());
            collectList.add(collect);
        }
        
        collectRepository.saveAll(collectList);
        return collectList.size();
    }

    @Transactional
    public int migrateLandCollects() {
        List<Collect> collectList = new ArrayList<>();
        List<LandCollect> landCollects = landCollectRepository.findAll();
        
        for (LandCollect lc : landCollects) {
            Collect collect = new Collect();
            collect.setCollectId(lc.getCollectId());
            collect.setLinkUrl(lc.getLinkUrl());
            collect.setUserId(lc.getUserId());
            collect.setCollectTime(lc.getCollectTime());
            collectList.add(collect);
        }
        
        collectRepository.saveAll(collectList);
        return collectList.size();
    }

    public long getPostCollectCount() {
        return postCollectRepository.count();
    }

    public long getLandCollectCount() {
        return landCollectRepository.count();
    }

    public long getCollectCount() {
        return collectRepository.count();
    }
}