package com.example.covid_app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid_app.R;
import com.example.covid_app.models.Prevention;

import java.util.ArrayList;

public class PreventionAdapter extends RecyclerView.Adapter<PreventionHolder> {

    Context context;
    ArrayList<Prevention> preventionList;

    public PreventionAdapter(Context context, ArrayList<Prevention> preventionList) {
        this.preventionList = preventionList;
        this.context = context;
    }

    @NonNull
    @Override
    public PreventionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        PreventionHolder preventionHolder = new PreventionHolder(LayoutInflater.from(context).inflate(
                R.layout.card_prevention,
                parent,
                false)
        );
        return preventionHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PreventionHolder holder, int position) {
        if(preventionList.get(position).getTitle().equals("Portez un masque")){
            holder.preventionCovert.setImageDrawable(context.getResources().getDrawable(R.drawable.facemask));
        }
        if(preventionList.get(position).getTitle().equals("Desinfectez frequemment les mains")){
            holder.preventionCovert.setImageDrawable(context.getResources().getDrawable(R.drawable.handwash));
        }
        if(preventionList.get(position).getTitle().equals("Toussez sous le pli du coude")){
            holder.preventionCovert.setImageDrawable(context.getResources().getDrawable(R.drawable.cough));
        }
        if(preventionList.get(position).getTitle().equals("Restez chez vous")){
            holder.preventionCovert.setImageDrawable(context.getResources().getDrawable(R.drawable.stay_home));
        }
        if(preventionList.get(position).getTitle().equals("Distanciation d'au moins 1 metres")){
            holder.preventionCovert.setImageDrawable(context.getResources().getDrawable(R.drawable.distance));
        }
        holder.preventionTitle.setText(preventionList.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return preventionList.size();
    }
}
