package com.example.memories;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class User {
    private String id;
    private String name;
    private Boolean onBoarding;
    private String email;
    private Album defaultAlbum;
    private Album privateAlbum;
    private Album favouriteAlbum;

    public User() {}

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.onBoarding = false;
    }

    public User(String id, String name, String email, Boolean onBoarding) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.onBoarding = onBoarding;
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Boolean getOnBoarding() { return onBoarding; }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOnBoarding(Boolean onBoarding) { this.onBoarding = onBoarding; }

    public Album getDefaultAlbum() {
        return defaultAlbum;
    }

    public Album getFavouriteAlbum() {
        return favouriteAlbum;
    }

    public Album getPrivateAlbum() {
        return privateAlbum;
    }

    public void setDefaultAlbum(Album defaultAlbum) {
        this.defaultAlbum = defaultAlbum;
    }

    public void setFavouriteAlbum(Album favouriteAlbum) {
        this.favouriteAlbum = favouriteAlbum;
    }

    public void setPrivateAlbum(Album privateAlbum) {
        this.privateAlbum = privateAlbum;
    }

    public void saveUser(Context content) {
        SharedPreferences sharedPref = content.getSharedPreferences("current_user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        Gson gson = new Gson();
        String json = gson.toJson(this);
        editor.putString("current_user", json);
        editor.apply();
    }

    public User getUser(Context content) {
        SharedPreferences sharedPref = content.getSharedPreferences("current_user", Context.MODE_PRIVATE);
        String data = sharedPref.getString("current_user", "");
        Gson gson = new Gson();
        User user = gson.fromJson(data, User.class);
        return user;
    }
}
