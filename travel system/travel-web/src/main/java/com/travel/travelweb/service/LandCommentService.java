package com.travel.travelweb.service;

import com.travel.travelweb.entity.LandComment;
import com.travel.travelweb.repo.LandCommentRepository;
import com.travel.travelweb.repo.SysUserRepository;
import com.travel.travelweb.util.IdGenerator;
import com.travel.travelweb.web.dto.LandCommentView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LandCommentService {

    private final LandCommentRepository landCommentRepository;
    private final SysUserRepository sysUserRepository;

    public LandCommentService(LandCommentRepository landCommentRepository, SysUserRepository sysUserRepository) {
        this.landCommentRepository = landCommentRepository;
        this.sysUserRepository = sysUserRepository;
    }

    public List<LandCommentView> listForLandscape(String landscapeId) {
        return landCommentRepository.findByLandscapeIdOrderByPublishTimeDesc(landscapeId).stream()
                .map(c -> {
                    String name = sysUserRepository.findById(c.getUserId())
                            .map(u -> u.getUserName() != null ? u.getUserName() : u.getUserId())
                            .orElse("用户");
                    return new LandCommentView(c.getCommentId(), c.getUserId(), name, c.getContent(), c.getPublishTime());
                })
                .toList();
    }

    @Transactional
    public void add(String landscapeId, String userId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("评论不能为空");
        }
        LandComment c = new LandComment();
        c.setCommentId(IdGenerator.next("LCM"));
        c.setLandscapeId(landscapeId);
        c.setUserId(userId);
        c.setContent(content.trim());
        c.setPublishTime(LocalDateTime.now());
        landCommentRepository.save(c);
    }

    public Optional<String> findLandscapeId(String commentId) {
        return landCommentRepository.findById(commentId).map(LandComment::getLandscapeId);
    }

    @Transactional
    public void deleteOwn(String commentId, String userId) {
        LandComment c = landCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));
        if (!userId.equals(c.getUserId())) {
            throw new IllegalArgumentException("无权删除该评论");
        }
        landCommentRepository.delete(c);
    }

    @Transactional
    public void updateOwn(String commentId, String userId, String content) {
        LandComment c = landCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));
        if (!userId.equals(c.getUserId())) {
            throw new IllegalArgumentException("无权修改该评论");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        c.setContent(content.trim());
        c.setPublishTime(LocalDateTime.now());
        landCommentRepository.save(c);
    }

    @Transactional
    public boolean deleteByAdmin(String commentId) {
        return landCommentRepository.findById(commentId).map(c -> {
            landCommentRepository.delete(c);
            return true;
        }).orElse(false);
    }
}
