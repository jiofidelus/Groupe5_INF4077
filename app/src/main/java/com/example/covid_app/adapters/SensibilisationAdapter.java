package com.example.covid_app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid_app.R;
import com.example.covid_app.models.SensibilisationMessage;

import java.util.ArrayList;

public class SensibilisationAdapter extends RecyclerView.Adapter<SensibilisationHolder> {

    Context context;
    ArrayList<SensibilisationMessage> messageList;

    public SensibilisationAdapter(Context context, ArrayList<SensibilisationMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public SensibilisationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SensibilisationHolder(LayoutInflater.from(context).inflate(R.layout.card_sensibilisation_line, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SensibilisationHolder holder, int position) {
        holder.cardViewClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage(position);
            }
        });
    }

    /**
     * This method show details for message at position on the recycler view
     * @param position is the position of the message into the recycler view
     */
    private void showMessage(int position) {
        // Open Bottom sheet and show details
    }

    /**
     * This hide bottom sheet for message details
     */
    private void hideMessage(){
        // Hide message
    }

    @Override
    public void onViewAttachedToWindow(@NonNull SensibilisationHolder holder) {
        super.onViewAttachedToWindow(holder);
        String image = null;
        if (messageList.get(holder.getAdapterPosition()) != null){
            if (messageList.get(holder.getAdapterPosition()).getImages() != null) {
                holder.simpleDraweeView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                image = messageList.get(holder.getAdapterPosition()).getImages().get(0);
            }
        }
        if (image != null) {
            holder.simpleDraweeView.setImageURI(image);
        }else {
            holder.simpleDraweeView.setImageResource(R.drawable.ic_baseline_image_not_supported_24);
            holder.simpleDraweeView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
        holder.sendDate.setText(messageList.get(holder.getAdapterPosition()).getSendDate());
        holder.senderMail.setText(messageList.get(holder.getAdapterPosition()).getSenderMail());
        holder.title.setText(messageList.get(holder.getAdapterPosition()).getTitle());
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull SensibilisationHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (messageList.get(holder.getAdapterPosition()) != null){
            if (messageList.get(holder.getAdapterPosition()).getImages() != null) {
                holder.simpleDraweeView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.simpleDraweeView.setImageResource(R.drawable.gradient_sensibilisation);
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.messageList.size();
    }
}
