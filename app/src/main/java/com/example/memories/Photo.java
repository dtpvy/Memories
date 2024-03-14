package com.example.memories;

import java.util.Date;

public class Photo {
    private String id;
    private String userId;
    private String imgUrl;
    private Date createdAt;
    private String historyId;

    public Photo() {}

    public Photo(String userId, String imgUrl, Date createdAt, String historyId) {
        this.userId = userId;
        this.historyId = historyId;
        this.imgUrl = imgUrl;
        this.createdAt = createdAt;
    }

    public Photo(String imgUrl, Date createdAt) {
        this.imgUrl = imgUrl;
        this.createdAt = createdAt;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHistoryId() {
        return historyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
