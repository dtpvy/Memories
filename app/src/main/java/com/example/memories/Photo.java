package com.example.memories;

import java.util.Date;

public class Photo {
    private String imgUrl;
    private Date createdAt;

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
}
