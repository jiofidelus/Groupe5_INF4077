package com.example.covid_app.adapters;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid_app.R;

public class HasScreenedHolder extends RecyclerView.ViewHolder {


    TextView hasScreenedName;
    TextView hasScreenedPhone;
    TextView hasScreenedStatus;

    RelativeLayout hasScreenedCard;

    public HasScreenedHolder(@NonNull View itemView) {
        super(itemView);

        hasScreenedName = itemView.findViewById(R.id.hasScreenedName);
        hasScreenedPhone = itemView.findViewById(R.id.hasScreenedPhone);
        hasScreenedStatus = itemView.findViewById(R.id.hasScreenedStatus);
        hasScreenedCard = itemView.findViewById(R.id.hasScreenedCard);

    }
}
