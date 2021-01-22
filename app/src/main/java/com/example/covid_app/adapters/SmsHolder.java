package com.example.covid_app.adapters;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid_app.R;

public class SmsHolder extends RecyclerView.ViewHolder {

    RelativeLayout relative_visibility;
    CardView card_view_sms;

    RelativeLayout card_background;


    LinearLayoutCompat isSms;
    AppCompatTextView message_user_name_sms;
    AppCompatTextView message;
    AppCompatTextView send_date_sms;


    LinearLayoutCompat isVoice;
    AppCompatTextView message_user_name_voice;
    AppCompatTextView voice_duration;
    SeekBar voice_progress;
    AppCompatImageView voice_stop;
    AppCompatImageView voice_play_pause;
    AppCompatTextView send_date_voice;


    public SmsHolder(@NonNull View itemView) {
        super(itemView);

        relative_visibility = itemView.findViewById(R.id.relative_visibility);
        card_view_sms = itemView.findViewById(R.id.card_view_sms);

        card_background = itemView.findViewById(R.id.card_background);

        isSms = itemView.findViewById(R.id.isSms);
        message_user_name_sms = itemView.findViewById(R.id.message_user_name_sms);
        message = itemView.findViewById(R.id.message);
        send_date_sms = itemView.findViewById(R.id.send_date_sms);


        isVoice = itemView.findViewById(R.id.isVoice);
        message_user_name_voice = itemView.findViewById(R.id.message_user_name_voice);
        voice_duration = itemView.findViewById(R.id.voice_duration);
        voice_progress = itemView.findViewById(R.id.voice_progress);
        voice_stop = itemView.findViewById(R.id.voice_stop);
        voice_play_pause = itemView.findViewById(R.id.voice_play_pause);
        send_date_voice = itemView.findViewById(R.id.send_date_voice);
    }
}
