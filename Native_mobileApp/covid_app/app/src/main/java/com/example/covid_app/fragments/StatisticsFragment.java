package com.example.covid_app.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.example.covid_app.R;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class StatisticsFragment extends Fragment {

    Context context;
    Activity activity;

    ScrollView statisticScrollView;

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
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getViews();
        OverScrollDecoratorHelper.setUpOverScroll(statisticScrollView);
    }

    void getViews(){
        statisticScrollView = activity.findViewById(R.id.statisticScrollView);
    }
}