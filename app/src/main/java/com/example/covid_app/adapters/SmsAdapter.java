package com.example.covid_app.adapters;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid_app.R;
import com.example.covid_app.models.SmsModel;

import java.io.IOException;
import java.util.ArrayList;

public class SmsAdapter extends RecyclerView.Adapter<SmsHolder> {

    Context context;
    AppCompatActivity activity;
    ArrayList<SmsModel> smsList;
    MediaPlayer mediaPlayer = new MediaPlayer();
    OnItemClickListener onItemClickListener;

    String userUID;

    boolean isPlaying = false;
    private String currentPath = null;
    private Handler handlePlayingVoice = new Handler();
    private Runnable runnablePlayVoice;

    public SmsAdapter(Context context, ArrayList<SmsModel> smsList, String userUID, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.smsList = smsList;
        this.onItemClickListener = onItemClickListener;
        this.userUID = userUID;
        activity = (AppCompatActivity) context;
    }

    @NonNull
    @Override
    public SmsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SmsHolder(LayoutInflater.from(context).inflate(R.layout.card_sms, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SmsHolder holder, int position) {
        checkContent(holder, position);
        updateUi(holder, position);
    }

    @Override
    public void onViewRecycled(@NonNull SmsHolder holder) {
        super.onViewRecycled(holder);
        updateUi(holder, holder.getAdapterPosition());
    }

    @Override
    public void onViewAttachedToWindow(@NonNull SmsHolder holder) {
        super.onViewAttachedToWindow(holder);
        updateUi(holder, holder.getAdapterPosition());
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull SmsHolder holder) {
        super.onViewDetachedFromWindow(holder);
        updateUi(holder, holder.getAdapterPosition());
    }


    private void updateUi(SmsHolder holder, int position){
        if (!onItemClickListener.currentPlaying(position)){
            holder.voice_play_pause.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24));
            resetProgress(holder);
        }else {
            if (isPlaying){
                holder.voice_play_pause.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_pause_24));
            }else {
                holder.voice_play_pause.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24));
            }
        }
    }

    private void checkContent(SmsHolder holder, int position) {
        if (smsList.get(position) == null){
            holder.card_view_sms.setVisibility(View.GONE);
            holder.relative_visibility.setVisibility(View.VISIBLE);
        }else {
            holder.card_view_sms.setVisibility(View.VISIBLE);
            holder.relative_visibility.setVisibility(View.GONE);
            // check if it me the sender of content
            if (userUID.equals(smsList.get(position).getUserUid())){
                // for sms
                holder.card_background.setBackgroundColor(context.getResources().getColor(R.color.me));
                holder.message_user_name_sms.setTextColor(context.getResources().getColor(R.color.mainColor));
                holder.send_date_sms.setTextColor(context.getResources().getColor(R.color.mainColor));
                holder.message.setTextColor(context.getResources().getColor(R.color.mainColor));
                // for voice
                holder.message_user_name_voice.setTextColor(context.getResources().getColor(R.color.mainColor));
                holder.send_date_voice.setTextColor(context.getResources().getColor(R.color.mainColor));
                holder.voice_duration.setTextColor(context.getResources().getColor(R.color.mainColor));
                holder.voice_stop.setColorFilter(context.getResources().getColor(R.color.mainColor));
                holder.voice_stop.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ripple_me));
                holder.voice_play_pause.setColorFilter(context.getResources().getColor(R.color.mainColor));
                holder.voice_play_pause.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ripple_me));
            }else {
                // for sms
                holder.card_background.setBackgroundColor(context.getResources().getColor(R.color.other));
                holder.message_user_name_sms.setTextColor(context.getResources().getColor(R.color.grey6));
                holder.send_date_sms.setTextColor(context.getResources().getColor(R.color.grey6));
                holder.message.setTextColor(context.getResources().getColor(R.color.grey6));
                // for voice
                holder.message_user_name_voice.setTextColor(context.getResources().getColor(R.color.grey6));
                holder.send_date_voice.setTextColor(context.getResources().getColor(R.color.grey6));
                holder.voice_duration.setTextColor(context.getResources().getColor(R.color.grey6));
                holder.voice_stop.setColorFilter(context.getResources().getColor(R.color.grey6));
                holder.voice_stop.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ripple_other));
                holder.voice_play_pause.setColorFilter(context.getResources().getColor(R.color.grey6));
                holder.voice_play_pause.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ripple_other));
            }
            // check if the content is message or voice
            if (smsList.get(position).isSMS()){
                // for message
                holder.isVoice.setAlpha(0);
                holder.isVoice.setVisibility(View.GONE);
                holder.isSms.setVisibility(View.VISIBLE);
                holder.isSms.setAlpha(1);
                if (smsList.get(position).getUserName() == null){
                    holder.message_user_name_sms.setText("<<inconnu>>");
                }else {
                    if (smsList.get(position).getUserName().isEmpty()){
                        holder.message_user_name_sms.setText("<<inconnu>>");
                    }else {
                        holder.message_user_name_sms.setText(smsList.get(position).getUserName());
                    }
                }
                holder.message.setText(smsList.get(position).getMessage());
                holder.send_date_sms.setText(smsList.get(position).getSendDate());
            }else {
                // for voice
                holder.isSms.setAlpha(0);
                holder.isSms.setVisibility(View.GONE);
                holder.isVoice.setVisibility(View.VISIBLE);
                holder.isVoice.setAlpha(1);
                if (smsList.get(position).getUserName() == null){
                    holder.message_user_name_voice.setText("<<inconnu>>");
                }else {
                    if (smsList.get(position).getUserName().isEmpty()){
                        holder.message_user_name_voice.setText("<<inconnu>>");
                    }else {
                        holder.message_user_name_voice.setText(smsList.get(position).getUserName());
                    }
                }
                String tempDuration = smsList.get(position).getVoiceDuration()+" sec";
                holder.voice_duration.setText(tempDuration);
                holder.send_date_voice.setText(smsList.get(position).getSendDate());
                holder.voice_play_pause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentPath = smsList.get(position).getVoiceUrl();
                        if (onItemClickListener.currentPlaying(position)){
                            //
                            isPlaying = !isPlaying;
                            updatePlayingUi(holder);
                            if (isPlaying){
                                resumePlayingVoice(holder, position);
                            }else {
                                pausePlayingVoice();
                            }
                        }else {
                            isPlaying = true;
                            updatePlayingUi(holder);
                            onItemClickListener.onItemClick(position);
                            playVoice(holder, position);
                        }
                    }
                });
                holder.voice_stop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPlaying = false;
                        updatePlayingUi(holder);
                        if (onItemClickListener.currentPlaying(position)){
                            onItemClickListener.onItemClick(-1);
                            stopPlayingVoice();
                            resetProgress(holder);
                        }
                    }
                });

            }
        }
    }

    private void updatePlayingUi(SmsHolder holder){
        if (isPlaying){
            holder.voice_play_pause.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_pause_24));
        }else {
            holder.voice_play_pause.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24));
        }
    }

    private void playVoice(SmsHolder holder, int position) {
        stopPlayingVoice();
        resetProgress(holder);
        mediaPlayer = new MediaPlayer();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        try {
            mediaPlayer.setDataSource(currentPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (onItemClickListener.currentPlaying(position)){
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            if (onItemClickListener.currentPlaying(position)){
                                mediaPlayer.start();
                                long duration = mediaPlayer.getDuration();
                                holder.voice_progress.setMax((int)(duration/100));
                                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        isPlaying = false;
                                        updatePlayingUi(holder);
                                        stopPlayingVoice();
                                        resetProgress(holder);
                                    }
                                });
                                runnablePlayVoice = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isPlaying && onItemClickListener.currentPlaying(position)){
                                            int temMediaPlayerProgress = mediaPlayer.getCurrentPosition();
                                            int progress = temMediaPlayerProgress/100;
                                            setProgress(holder, progress);
                                            handlePlayingVoice.postDelayed(this, 100);
                                        }
                                    }
                                };
                                //Start
                                handlePlayingVoice.postDelayed(runnablePlayVoice, 100);
                            }
                        }
                    });
                }
            }
        }).start();
    }

    private void resumePlayingVoice(SmsHolder holder, int position){
        if (mediaPlayer == null) {
            playVoice(holder, position);
        }else {
            if (onItemClickListener.currentPlaying(position)) {
                mediaPlayer.start();
                long duration = mediaPlayer.getDuration();
                holder.voice_progress.setMax((int) (duration / 100));
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        isPlaying = false;
                        updatePlayingUi(holder);
                        stopPlayingVoice();
                        resetProgress(holder);
                    }
                });
                runnablePlayVoice = new Runnable() {
                    @Override
                    public void run() {
                        if (isPlaying && onItemClickListener.currentPlaying(position)) {
                            int temMediaPlayerProgress = mediaPlayer.getCurrentPosition();
                            int progress = temMediaPlayerProgress / 100;
                            setProgress(holder, progress);
                            handlePlayingVoice.postDelayed(this, 100);
                        }
                    }
                };
                //Start
                handlePlayingVoice.postDelayed(runnablePlayVoice, 100);
            }
        }
    }

    private void pausePlayingVoice(){
        if (mediaPlayer != null){
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
    }

    private void stopPlayingVoice(){
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = null;
    }

    private void setProgress(SmsHolder smsHolder, int progress){
        if (isPlaying){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                smsHolder.voice_progress.setProgress(progress, true);
            }else {
                smsHolder.voice_progress.setProgress(progress);
            }
        }
    }

    private void resetProgress(SmsHolder holder){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.voice_progress.setProgress(0, true);
        }else {
            holder.voice_progress.setProgress(0);
        }
    }

    @Override
    public int getItemCount() {
        return smsList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int currentPosition);
        boolean currentPlaying(int position);
    }
}
