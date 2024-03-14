package com.example.memories;

import java.util.Map;

public class Album {
    private String name;
    private String userId;
    private String imgUrl;
    private Map<String, Photo> photos;

    public Album(String imgUrl, String name) {
        this.name = name;
        this.imgUrl = imgUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }
    public String getName() { return name; }
}
