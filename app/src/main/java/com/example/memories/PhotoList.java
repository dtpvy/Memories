package com.example.memories;

import java.util.ArrayList;
import java.util.Date;

public class PhotoList {
    private Date date;
    private ArrayList<Media> media;

    public PhotoList(Date date) {
        this.date = date;
        this.media = new ArrayList<Media>();
    }

    public PhotoList(Date date, ArrayList<Media> media) {
        this.date = date;
        this.media = media;
    }


    public Date getDate() {
        return date;
    }

    public ArrayList<Media> getPhotos() {
        return media;
    }
}
