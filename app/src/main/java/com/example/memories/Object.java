package com.example.memories;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Object {
    private String id;
    private String name;
    private String userId;
    private String imgUrl;
    private ArrayList<Media> photos;
    private String label;

    public  Object() {}

    public Object(String userId) {
        this.id = UUID.randomUUID().toString();
        this.name = "Unknown";
        this.label = "Unknown";
        this.userId = userId;
        this.photos = new ArrayList<>();
    }

    public Object(String userId, String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.label = name;
        this.userId = userId;
        this.photos = new ArrayList<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setPhotos(ArrayList<Media> photos) {
        this.photos = photos;
    }

    public ArrayList<Media> getPhotos() {
        return photos;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
