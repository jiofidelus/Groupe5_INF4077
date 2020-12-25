package com.example.covid_app.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid_app.R;

public class PreventionHolder extends RecyclerView.ViewHolder {

    ImageView preventionCovert;
    TextView preventionTitle;

    public PreventionHolder(@NonNull View itemView) {
        super(itemView);

        preventionCovert = itemView.findViewById(R.id.preventionImage);
        preventionTitle = itemView.findViewById(R.id.preventionTitle);
    }
}
