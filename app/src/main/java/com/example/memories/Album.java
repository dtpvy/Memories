package com.example.memories;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class Album {
    private String id;
    private String name;
    private String userId;
    private String imgUrl;
    private Boolean isMutate;
    private ArrayList<Photo> photos;
    private Date createdAt;

    public  Album() {}

    public Album(String userId) {
        this.id = UUID.randomUUID().toString();
        this.name = "Tất cả";
        this.userId = userId;
        this.isMutate = false;
        this.photos = new ArrayList<>();
        this.createdAt = new Date();
    }

    public Album(String imgUrl, String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.imgUrl = imgUrl;
        this.isMutate = true;
        this.photos = new ArrayList<>();
        this.createdAt = new Date();
    }

    public Album(String userId, String imgUrl, String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.imgUrl = imgUrl;
        this.userId = userId;
        this.isMutate = true;
        this.photos = new ArrayList<>();
        this.createdAt = new Date();
    }

    public Album(String userId, String imgUrl, String name, ArrayList<Photo> photos) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.imgUrl = imgUrl;
        this.userId = userId;
        this.isMutate = true;
        this.photos = photos;
        this.createdAt = new Date();
    }

    public Album(String userId, String imgUrl, String name, Boolean isMutate) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.imgUrl = imgUrl;
        this.userId = userId;
        this.isMutate = isMutate;
        this.photos = new ArrayList<>();
        this.createdAt = new Date();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<Photo> photos) {
        this.photos = photos;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getName() { return name; }

    public Boolean getMutate() {
        return isMutate;
    }

    public void setMutate(Boolean mutate) {
        isMutate = mutate;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
