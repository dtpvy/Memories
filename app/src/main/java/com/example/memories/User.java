package com.example.memories;

public class User {
    private String id;
    private String name;
    private Boolean onBoarding;
    private String email;

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
}
