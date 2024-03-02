package com.example.memories;

import java.util.ArrayList;
import java.util.Date;

public class PhotoList {
    private Date date;
    private ArrayList<Photo> photos;

    public PhotoList(Date date) {
        this.date = date;
        this.photos = new ArrayList<Photo>();
    }

    public PhotoList(Date date, ArrayList<Photo> photos) {
        this.date = date;
        this.photos = photos;
    }


    public Date getDate() {
        return date;
    }

    public ArrayList<Photo> getPhotos() {
        return photos;
    }
}
