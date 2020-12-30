package com.example.covid_app.models;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class User {

    String id;
    String username;
    Boolean isManager = false;
    @Nullable private String picture;
    List<HasScreened> citizensScreened = new ArrayList<>();

    public User(String id, String username, @Nullable String picture) {
        this.id = id;
        this.username = username;
        this.picture = picture;
    }
}
