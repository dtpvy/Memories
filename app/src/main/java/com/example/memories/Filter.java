package com.example.memories;

import android.graphics.Bitmap;

import ja.burhanrashid52.photoeditor.PhotoFilter;

public class Filter {
    private String name;
    private PhotoFilter photoFilter;
    private Bitmap bitmap;

    public Filter(String name, PhotoFilter photoFilter) {
        this.name = name;
        this.photoFilter = photoFilter;
    }

    public String getName() {
        return name;
    }

    public PhotoFilter getPhotoFilter() {
        return photoFilter;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
