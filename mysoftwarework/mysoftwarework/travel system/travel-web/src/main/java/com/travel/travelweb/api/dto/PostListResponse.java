package com.travel.travelweb.api.dto;

import java.util.List;

public class PostListResponse {
    private List<PostResponse> posts;
    private long total;

    public PostListResponse() {}

    public PostListResponse(List<PostResponse> posts, long total) {
        this.posts = posts;
        this.total = total;
    }

    public List<PostResponse> getPosts() { return posts; }
    public void setPosts(List<PostResponse> posts) { this.posts = posts; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
}
