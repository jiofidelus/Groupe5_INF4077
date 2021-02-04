package com.example.covid_app.adapters;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid_app.R;
import com.facebook.drawee.view.SimpleDraweeView;

public class SensibilisationHolder extends RecyclerView.ViewHolder {

    CardView cardViewClick;
    SimpleDraweeView simpleDraweeView;
    AppCompatTextView title;
    AppCompatTextView senderMail;
    AppCompatTextView sendDate;

    /**
     * This method is the constructor for the holder who will pass item view to inflate in the recycler view
     * @param itemView is the view to inflate into the recycler view
     */
    public SensibilisationHolder(@NonNull View itemView) {
        super(itemView);
        cardViewClick = itemView.findViewById(R.id.cardViewClick);
        simpleDraweeView = itemView.findViewById(R.id.simpleDraweeView);
        title = itemView.findViewById(R.id.title);
        senderMail = itemView.findViewById(R.id.senderMail);
        sendDate = itemView.findViewById(R.id.sendDate);
    }
}
