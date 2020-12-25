package com.example.covid_app.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.example.covid_app.R;
import com.example.covid_app.adapters.PreventionAdapter;
import com.example.covid_app.adapters.SymptomAdapter;
import com.example.covid_app.models.Prevention;
import com.example.covid_app.models.Symptom;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.producers.PostprocessedBitmapMemoryCacheProducer;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;

import jp.wasabeef.fresco.processors.BlurPostprocessor;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class HomeFragment extends Fragment {

    Context context;
    Activity activity;

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
        context = requireContext();
        activity = requireActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getViews();
        symptomRecyclerView.setNestedScrollingEnabled(false);
        preventionRecyclerView.setNestedScrollingEnabled(false);
        OverScrollDecoratorHelper.setUpOverScroll(scrollView);
        getPreventionList();
        getSymptomList();
        setupAdapter();
    }

    void getViews(){
        preventionRecyclerView = activity.findViewById(R.id.preventionRecyclerView);
        symptomRecyclerView = activity.findViewById(R.id.symptomRecyclerView);
        scrollView = activity.findViewById(R.id.scrollView);
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