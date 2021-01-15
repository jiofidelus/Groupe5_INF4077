package com.example.covid_app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid_app.R;
import com.example.covid_app.adapters.PreventionAdapter;
import com.example.covid_app.adapters.SymptomAdapter;
import com.example.covid_app.models.Prevention;
import com.example.covid_app.models.Symptom;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class HomeFragment extends Fragment {

    Context context;
    AppCompatActivity activity;

    RecyclerView preventionRecyclerView;
    RecyclerView symptomRecyclerView;
    PreventionAdapter preventionAdapter;
    SymptomAdapter symptomAdapter;

    ArrayList<Prevention> preventionList = new ArrayList<>();
    ArrayList<Symptom> symptomList = new ArrayList<>();

    ScrollView scrollView;

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result =  inflater.inflate(R.layout.fragment_home, container, false);

        context = result.getContext();
        activity = (AppCompatActivity) context;

        initView(result);
        symptomRecyclerView.setNestedScrollingEnabled(false);
        preventionRecyclerView.setNestedScrollingEnabled(false);
        OverScrollDecoratorHelper.setUpOverScroll(scrollView);
        getPreventionList();
        getSymptomList();
        setupAdapter();
        return result;
    }

    void initView(View view){
        preventionRecyclerView = view.findViewById(R.id.preventionRecyclerView);
        symptomRecyclerView = view.findViewById(R.id.symptomRecyclerView);
        scrollView = view.findViewById(R.id.scrollView);
    }

    void setupAdapter(){
        FlexboxLayoutManager preventionLayoutManager = new FlexboxLayoutManager(context);
        preventionLayoutManager.setFlexDirection(FlexDirection.ROW);
        preventionLayoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
        preventionRecyclerView.setLayoutManager(preventionLayoutManager);
        preventionAdapter = new PreventionAdapter(context, preventionList);
        preventionRecyclerView.setAdapter(preventionAdapter);

        FlexboxLayoutManager symptomLayoutManager = new FlexboxLayoutManager(context);
        symptomLayoutManager.setFlexDirection(FlexDirection.ROW);
        symptomLayoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
        symptomRecyclerView.setLayoutManager(symptomLayoutManager);
        symptomAdapter = new SymptomAdapter(context, symptomList);
        symptomRecyclerView.setAdapter(symptomAdapter);
    }

    void getPreventionList(){
        Prevention prevention = new Prevention();
        prevention.setTitle("Portez un masque");
        prevention.setImage(null);
        preventionList.add(prevention);

        prevention = new Prevention();
        prevention.setTitle("Desinfectez frequemment les mains");
        prevention.setImage(null);
        preventionList.add(prevention);

        prevention = new Prevention();
        prevention.setTitle("Toussez sous le pli du coude");
        prevention.setImage(null);
        preventionList.add(prevention);

        prevention = new Prevention();
        prevention.setTitle("Restez chez vous");
        prevention.setImage(null);
        preventionList.add(prevention);

        prevention = new Prevention();
        prevention.setTitle("Distanciation d'au moins 1 metres");
        prevention.setImage(null);
        preventionList.add(prevention);
    }

    void getSymptomList(){
        Symptom symptom = new Symptom();
        symptom.setTitle("Frequent : fievre");
        symptom.setImage(null);
        symptomList.add(symptom);

//        symptom = new Symptom();
//        symptom.setTitle("Frequent : toux seche");
//        symptom.setImage(null);
//        symptomList.add(symptom);

        symptom = new Symptom();
        symptom.setTitle("Frequent : fatigue");
        symptom.setImage(null);
        symptomList.add(symptom);

//        symptom = new Symptom();
//        symptom.setTitle("Moins frequent : courbatures");
//        symptom.setImage(null);
//        symptomList.add(symptom);

        symptom = new Symptom();
        symptom.setTitle("Moins frequent : maux de gorge");
        symptom.setImage(null);
        symptomList.add(symptom);

        symptom = new Symptom();
        symptom.setTitle("Moins frequent : maux de tete");
        symptom.setImage(null);
        symptomList.add(symptom);

//        symptom = new Symptom();
//        symptom.setTitle("Moins frequent : perte d'odorat ou du gout");
//        symptom.setImage(null);
//        symptomList.add(symptom);

        symptom = new Symptom();
        symptom.setTitle("Grave : difficultes a respirer");
        symptom.setImage(null);
        symptomList.add(symptom);
//
//        symptom = new Symptom();
//        symptom.setTitle("Grave : douleurs au niveau de la poitrine");
//        symptom.setImage(null);
//        symptomList.add(symptom);
//
//        symptom = new Symptom();
//        symptom.setTitle("Grave : perte de motricite");
//        symptom.setImage(null);
//        symptomList.add(symptom);
    }
}