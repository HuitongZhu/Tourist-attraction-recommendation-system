package com.travel.travelweb.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travel.travelweb.entity.Comment;
import com.travel.travelweb.entity.LandComment;
import com.travel.travelweb.entity.PostComment;
import com.travel.travelweb.repo.CommentRepository;
import com.travel.travelweb.repo.LandCommentRepository;
import com.travel.travelweb.repo.PostCommentRepository;

@Service
public class CommentMigrationService {

    private final PostCommentRepository postCommentRepository;
    private final LandCommentRepository landCommentRepository;
    private final CommentRepository commentRepository;

    public CommentMigrationService(PostCommentRepository postCommentRepository, 
                                   LandCommentRepository landCommentRepository,
                                   CommentRepository commentRepository) {
        this.postCommentRepository = postCommentRepository;
        this.landCommentRepository = landCommentRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public int migrateAllComments() {
        List<Comment> commentList = new ArrayList<>();
        
        List<PostComment> postComments = postCommentRepository.findAll();
        for (PostComment pc : postComments) {
            Comment comment = new Comment(pc.getCommentId(), pc.getUserId(), pc.getContent(), pc.getPublishTime());
            commentList.add(comment);
        }
        
        List<LandComment> landComments = landCommentRepository.findAll();
        for (LandComment lc : landComments) {
            Comment comment = new Comment(lc.getCommentId(), lc.getUserId(), lc.getContent(), lc.getPublishTime());
            commentList.add(comment);
        }
        
        commentRepository.saveAll(commentList);
        return commentList.size();
    }

    @Transactional
    public int migratePostComments() {
        List<Comment> commentList = new ArrayList<>();
        List<PostComment> postComments = postCommentRepository.findAll();
        for (PostComment pc : postComments) {
            Comment comment = new Comment(pc.getCommentId(), pc.getUserId(), pc.getContent(), pc.getPublishTime());
            commentList.add(comment);
        }
        commentRepository.saveAll(commentList);
        return commentList.size();
    }

    @Transactional
    public int migrateLandComments() {
        List<Comment> commentList = new ArrayList<>();
        List<LandComment> landComments = landCommentRepository.findAll();
        for (LandComment lc : landComments) {
            Comment comment = new Comment(lc.getCommentId(), lc.getUserId(), lc.getContent(), lc.getPublishTime());
            commentList.add(comment);
        }
        commentRepository.saveAll(commentList);
        return commentList.size();
    }

    public long countPostComments() {
        return postCommentRepository.count();
    }

    public long countLandComments() {
        return landCommentRepository.count();
    }

    public long countComments() {
        return commentRepository.count();
    }
}