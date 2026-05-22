package com.travel.travelweb.api.dto;

public class PostRequest {
    private String title;
    private String landscapeId;
    private String tag;
    private String content;

    public PostRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLandscapeId() { return landscapeId; }
    public void setLandscapeId(String landscapeId) { this.landscapeId = landscapeId; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
