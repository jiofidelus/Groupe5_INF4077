package com.example.covid_app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid_app.R;
import com.example.covid_app.models.Symptom;

import java.util.ArrayList;

public class SymptomAdapter extends RecyclerView.Adapter<SymptomHolder> {

    Context context;
    ArrayList<Symptom> symptomList;

    public SymptomAdapter(Context context, ArrayList<Symptom> symptomList) {
        this.symptomList = symptomList;
        this.context = context;
    }

    @NonNull
    @Override
    public SymptomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        SymptomHolder symptomHolder = new SymptomHolder(LayoutInflater.from(context).inflate(
                R.layout.card_symptoms,
                parent,
                false)
        );
        return symptomHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SymptomHolder holder, int position) {
        if(symptomList.get(position).getTitle().equals("Frequent : fievre")){
            holder.symptomCovert.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_fever));
        }
        if(symptomList.get(position).getTitle().equals("Frequent : fatigue")){
            holder.symptomCovert.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_nap));
        }
        if(symptomList.get(position).getTitle().equals("Moins frequent : maux de gorge")){
            holder.symptomCovert.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_sore_throat));
        }
        if(symptomList.get(position).getTitle().equals("Moins frequent : maux de tete")){
            holder.symptomCovert.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_head));
        }
        if(symptomList.get(position).getTitle().equals("Grave : difficultes a respirer")){
            holder.symptomCovert.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_lungs));
        }
        holder.symptomTitle.setText(symptomList.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return symptomList.size();
    }
}
