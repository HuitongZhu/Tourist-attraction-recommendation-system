package com.travel.travelweb.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travel.travelweb.entity.Comment;
import com.travel.travelweb.entity.LandComment;
import com.travel.travelweb.repo.CommentRepository;
import com.travel.travelweb.repo.LandCommentRepository;
import com.travel.travelweb.repo.SysUserRepository;
import com.travel.travelweb.util.IdGenerator;
import com.travel.travelweb.web.dto.LandCommentView;

@Service
public class LandCommentService {

    private final LandCommentRepository landCommentRepository;
    private final SysUserRepository sysUserRepository;
    private final CommentRepository commentRepository;

    public LandCommentService(LandCommentRepository landCommentRepository, SysUserRepository sysUserRepository, CommentRepository commentRepository) {
        this.landCommentRepository = landCommentRepository;
        this.sysUserRepository = sysUserRepository;
        this.commentRepository = commentRepository;
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
        String commentId = IdGenerator.next("LCM");
        LandComment c = new LandComment();
        c.setCommentId(commentId);
        c.setLandscapeId(landscapeId);
        c.setUserId(userId);
        c.setContent(content.trim());
        c.setPublishTime(LocalDateTime.now());
        landCommentRepository.save(c);
        
        Comment comment = new Comment();
        comment.setCommentId(commentId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        comment.setPublishTime(LocalDateTime.now());
        commentRepository.save(comment);
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
        commentRepository.deleteById(commentId);
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
        
        commentRepository.findById(commentId).ifPresent(comment -> {
            comment.setContent(content.trim());
            comment.setPublishTime(LocalDateTime.now());
            commentRepository.save(comment);
        });
    }

    @Transactional
    public boolean deleteByAdmin(String commentId) {
        return landCommentRepository.findById(commentId).map(c -> {
            landCommentRepository.delete(c);
            commentRepository.deleteById(commentId);
            return true;
        }).orElse(false);
    }
}
