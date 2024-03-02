package com.example.memories;

public class DiscoverHomeView {
    private String imgUrl;
    private String name;

    public DiscoverHomeView(String imgUrl, String name) {
        this.name = name;
        this.imgUrl = imgUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }
    public String getName() { return name; }
}
