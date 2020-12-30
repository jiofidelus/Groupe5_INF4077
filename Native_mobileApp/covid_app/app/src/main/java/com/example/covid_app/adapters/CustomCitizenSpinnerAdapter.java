package com.example.covid_app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.covid_app.R;
import com.example.covid_app.models.Citizen;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

public class CustomCitizenSpinnerAdapter extends ArrayAdapter<Citizen> {

    public CustomCitizenSpinnerAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Citizen> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent){
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_citizen_spinner, parent, false);
        }
        SimpleDraweeView simpleDraweeView = convertView.findViewById(R.id.citizenImage);
        TextView textViewName = convertView.findViewById(R.id.citizenName);
        TextView textViewContact = convertView.findViewById(R.id.citizenContact);

        Citizen currentCitizen = getItem(position);

        if (currentCitizen != null) {
            simpleDraweeView.setImageURI(currentCitizen.getUrlPicture());
            String tempName = currentCitizen.getFirstName() + " " + currentCitizen.getSecondName();
            textViewName.setText(tempName);
            String tempPhone = currentCitizen.getMobilePhone() + "";
            textViewContact.setText(tempPhone);
        }
        return convertView;
    }
}
