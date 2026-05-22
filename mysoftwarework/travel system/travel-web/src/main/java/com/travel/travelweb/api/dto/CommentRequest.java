package com.travel.travelweb.api.dto;

public class CommentRequest {
    
    private String landscapeId;
    private String postId;
    private String content;

    public CommentRequest() {
    }

    public CommentRequest(String landscapeId, String postId, String content) {
        this.landscapeId = landscapeId;
        this.postId = postId;
        this.content = content;
    }

    public String getLandscapeId() {
        return landscapeId;
    }

    public void setLandscapeId(String landscapeId) {
        this.landscapeId = landscapeId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
