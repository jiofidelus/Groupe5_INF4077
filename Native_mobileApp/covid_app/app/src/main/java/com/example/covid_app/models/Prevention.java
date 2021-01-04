package com.example.covid_app.models;

import android.graphics.Bitmap;

public class Prevention {

    String title;
    Bitmap image;

    public Prevention() {
    }

    public Prevention(String title, Bitmap image) {
        this.title = title;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}
