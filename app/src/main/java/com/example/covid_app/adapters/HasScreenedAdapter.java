package com.example.covid_app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid_app.R;
import com.example.covid_app.models.HasScreened;

import java.util.ArrayList;

public class HasScreenedAdapter extends RecyclerView.Adapter<HasScreenedHolder> {

    Context context;
    ArrayList<HasScreened> hasScreenedList;

    public HasScreenedAdapter(Context context, ArrayList<HasScreened> hasScreenedList) {
        this.context = context;
        this.hasScreenedList = hasScreenedList;
    }

    @NonNull
    @Override
    public HasScreenedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        HasScreenedHolder hasScreenedHolder = new HasScreenedHolder(LayoutInflater.from(context).inflate(
                R.layout.card_has_screened,
                parent,
                false)
        );
        return hasScreenedHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HasScreenedHolder holder, int position) {
        String tempName = hasScreenedList.get(position).getCitizen_who_has_been_screened().getFirstName() + " " + hasScreenedList.get(position).getCitizen_who_has_been_screened().getSecondName();
        holder.hasScreenedName.setText(tempName);
        String tempPhone = hasScreenedList.get(position).getCitizen_who_has_been_screened().getMobilePhone()+"";
        holder.hasScreenedPhone.setText(tempPhone);
        String tempStatus = hasScreenedList.get(position).getStatus();
        holder.hasScreenedStatus.setText(tempStatus);
        if (tempStatus.equals("+")){
            holder.hasScreenedStatus.setTextColor(context.getResources().getColor(R.color.orange));
        }
        if (tempStatus.equals("-")){
            holder.hasScreenedStatus.setTextColor(context.getResources().getColor(R.color.blueLight));
        }
        if (tempStatus.equals("?")){
            holder.hasScreenedStatus.setTextColor(context.getResources().getColor(R.color.grey4));
        }
        if (tempStatus.equals("R")){
            holder.hasScreenedStatus.setTextColor(context.getResources().getColor(R.color.green));
        }
        if (tempStatus.equals("D")){
            holder.hasScreenedStatus.setTextColor(context.getResources().getColor(R.color.red));
        }

        holder.hasScreenedInfoButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });
    }

    @Override
    public int getItemCount() {
        return hasScreenedList.size();
    }

}
