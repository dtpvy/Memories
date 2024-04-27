package com.example.memories;

import android.net.Uri;

public class CustomObject {
    private  Uri uri;
    private  Media media;

    public CustomObject(Uri uri, Media media) {
        this.media = media;
        this.uri = uri;
    }

    public Media getMedia() {
        return media;
    }

    public Uri getUri() {
        return uri;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
