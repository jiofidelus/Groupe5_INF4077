package com.example.covid_app.adapters;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid_app.R;

public class CitizenHolder extends RecyclerView.ViewHolder {

    TextView citizenName;
    TextView citizenPhone;

    RelativeLayout citizenInfoButtonClick;

    RelativeLayout citizenCard;

    public CitizenHolder(@NonNull View itemView) {
        super(itemView);

        citizenName = itemView.findViewById(R.id.citizenName);
        citizenPhone = itemView.findViewById(R.id.citizenPhone);

        citizenInfoButtonClick = itemView.findViewById(R.id.citizenInfoButtonClick);
        citizenCard = itemView.findViewById(R.id.citizenCard);
    }
}
