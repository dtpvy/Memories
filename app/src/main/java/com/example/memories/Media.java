package com.example.memories;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Media {
    private String id;
    private String userId;
    private String imgUrl;
    private String type;
    private Date createdAt;
    private String historyId;
    private Date deletedAt;
    private ArrayList<String> labels;

    public Media() {}

    public Media(String userId, String imgUrl, Date createdAt, String historyId, String type) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.historyId = historyId;
        this.imgUrl = imgUrl;
        this.createdAt = createdAt;
        this.type = type;
        this.labels = new ArrayList<>();
    }

    public Media(String userId, Date createdAt, String historyId, String type) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.historyId = historyId;
        this.createdAt = createdAt;
        this.type = type;
        this.labels = new ArrayList<>();
    }

    public Media(String imgUrl, Date createdAt, String type) {
        this.id = UUID.randomUUID().toString();
        this.imgUrl = imgUrl;
        this.createdAt = createdAt;
        this.type = type;
        this.labels = new ArrayList<>();
    }

    public Media(String userId, Date createdAt) {
        this.id = UUID.randomUUID().toString();
        this.createdAt = createdAt;
        this.userId = userId;
        this.labels = new ArrayList<>();
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

    public void setDeletedAt(Date date) {this.deletedAt = date; }

    public Date getDeletedAt() { return this.deletedAt; }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isVideo() {
        return this.type.contains("video");
    }

    public ArrayList<String> getLabels() {
        return labels;
    }

    public void setLabels(ArrayList<String> labels) {
        this.labels = labels;
    }
}
