package com.example.memories;

import java.util.ArrayList;
import java.util.Date;

public class PhotoList {
    private Date date;
    private String albumId;
    private ArrayList<Media> media;

    public PhotoList(Date date, String albumId) {
        this.date = date;
        this.albumId = albumId;
        this.media = new ArrayList<Media>();
    }

    public PhotoList(Date date, ArrayList<Media> media) {
        this.date = date;
        this.media = media;
    }

    public PhotoList(Date date, String albumId, ArrayList<Media> media) {
        this.date = date;
        this.albumId = albumId;
        this.media = media;
    }

    public String getAlbumId() {
        return albumId;
    }

    public Date getDate() {
        return date;
    }

    public ArrayList<Media> getPhotos() {
        return media;
    }
}
