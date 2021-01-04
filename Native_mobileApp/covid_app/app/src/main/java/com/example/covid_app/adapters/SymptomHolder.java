package com.example.covid_app.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid_app.R;

public class SymptomHolder extends RecyclerView.ViewHolder {
    ImageView symptomCovert;
    TextView symptomTitle;

    public SymptomHolder(@NonNull View itemView) {
        super(itemView);
        symptomCovert = itemView.findViewById(R.id.symptomImage);
        symptomTitle = itemView.findViewById(R.id.symptomTitle);
    }
}
