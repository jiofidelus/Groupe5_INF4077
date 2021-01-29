package com.example.covid_app.models;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class User {

    String id;
    String username;
    @Nullable private String picture;

    public User(String id, String username, @Nullable String picture) {
        this.id = id;
        this.username = username;
        this.picture = picture;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Nullable
    public String getPicture() {
        return picture;
    }

    public void setPicture(@Nullable String picture) {
        this.picture = picture;
    }
}
