package com.example.covid_app.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.example.covid_app.R;
import com.example.covid_app.models.HasScreened;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Objects;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class StatisticsFragment extends Fragment {

    Context context;
    AppCompatActivity activity;

    ScrollView statisticScrollView;

    PieChart pieChart;
    HorizontalBarChart barChart;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    FirebaseStorage firebaseStorage;
    float maxValue = 0;

    AppCompatTextView statsDead;
    AppCompatTextView statsActive;
    AppCompatTextView statsRecovered;
    AppCompatTextView statsPositive;

    Thread threadGetList;

    ArrayList<HasScreened> hasScreenedList = new ArrayList<>();
    final String regions[] = {"Centre ","Adamaoua","Est","Sud", "Nord", "Extreme-Nord", "Ouest", "Nord-Ouest", "Littoral", "Sud-Ouest"};
    final float[] regionValues = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private float totalCases = 0;
    private float activeCases = 0;
    private float recoveredCases = 0;
    private float deadCases = 0;
    private boolean canRunThread;
    private RelativeLayout chartContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result =  inflater.inflate(R.layout.fragment_statistics, container, false);
        canRunThread = true;
        context = result.getContext();
        activity = (AppCompatActivity) context;

        initView(result);
        OverScrollDecoratorHelper.setUpOverScroll(statisticScrollView);
        setupCharts();
        chartContainer.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fall_down));
        getHasScreenedList();
        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        canRunThread = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        canRunThread = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        canRunThread = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        canRunThread = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        canRunThread = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        canRunThread = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        canRunThread = false;
    }

    void getHasScreenedList(){
        threadGetList = new Thread(new Runnable() {
            @Override
            public void run() {
                if (canRunThread){
                    db.collection("hasscreeneds").addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                return;
                            }
                            for (DocumentSnapshot doc : Objects.requireNonNull(value)) {
                                HasScreened hasScreened = doc.toObject(HasScreened.class);
                                if (hasScreened != null){
                                    tryToAddHasScreened(hasScreened);
                                }
                            }
                            setValuesForRegions();
                        }
                    });
                }
            }
        });
        threadGetList.start();
    }

    private void tryToAddHasScreened(HasScreened hasScreened) {
        for (HasScreened hasScreened1 : hasScreenedList){
            if (hasScreened1 != null){
                if (hasScreened.getStatus().equals(hasScreened1.getStatus())){
                    if (hasScreened.getRegion().equals(hasScreened1.getRegion())){
                        if (hasScreened.getCitizen_who_has_been_screened().equals(hasScreened1.getCitizen_who_has_been_screened())){
                            if (hasScreened.getCity().equals(hasScreened1.getCity())){
                                if (hasScreened.getDepartment().equals(hasScreened1.getDepartment())){
                                    if (hasScreened.getQuarter().equals(hasScreened1.getQuarter())){
                                        if (hasScreened.getScreening_date().equals(hasScreened1.getScreening_date())){
                                            if (hasScreened.getType_screening().equals(hasScreened1.getType_screening())){
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        hasScreenedList.add(hasScreened);
    }

    private void setValuesForRegions(){
        for (HasScreened hasScreened: hasScreenedList){
            if (hasScreened != null){
                if (hasScreened.getStatus().equals(context.getResources().getString(R.string.active)) ||
                        hasScreened.getStatus().equals(context.getResources().getString(R.string.recovered)) ||
                        hasScreened.getStatus().equals(context.getResources().getString(R.string.dead))
                ){
                    if (hasScreened.getRegion().equals("Centre")){
                        regionValues[0] += 1;
                        if (maxValue < regionValues[0]){
                            maxValue = regionValues[0];
                        }
                    }
                    if (hasScreened.getRegion().equals("Adamaoua")){
                        regionValues[1] += 1;
                        if (maxValue < regionValues[1]){
                            maxValue = regionValues[1];
                        }
                    }
                    if (hasScreened.getRegion().equals("Est")){
                        regionValues[2] += 1;
                        if (maxValue < regionValues[2]){
                            maxValue = regionValues[2];
                        }
                    }
                    if (hasScreened.getRegion().equals("Sud")){
                        regionValues[3] += 1;
                        if (maxValue < regionValues[3]){
                            maxValue = regionValues[3];
                        }
                    }
                    if (hasScreened.getRegion().equals("Nord")){
                        regionValues[4] += 1;
                        if (maxValue < regionValues[4]){
                            maxValue = regionValues[4];
                        }
                    }
                    if (hasScreened.getRegion().equals("Extreme-Nord")){
                        regionValues[5] += 1;
                        if (maxValue < regionValues[5]){
                            maxValue = regionValues[5];
                        }
                    }
                    if (hasScreened.getRegion().equals("Ouest")){
                        regionValues[6] += 1;
                        if (maxValue < regionValues[6]){
                            maxValue = regionValues[6];
                        }
                    }
                    if (hasScreened.getRegion().equals("Nord-Ouest")){
                        regionValues[7] += 1;
                        if (maxValue < regionValues[7]){
                            maxValue = regionValues[7];
                        }
                    }
                    if (hasScreened.getRegion().equals("Littoral")){
                        regionValues[8] += 1;
                        if (maxValue < regionValues[8]){
                            maxValue = regionValues[8];
                        }
                    }
                    if (hasScreened.getRegion().equals("Sud-Ouest")){
                        regionValues[9] += 1;
                        if (maxValue < regionValues[9]){
                            maxValue = regionValues[9];
                        }
                    }
                }
                if (hasScreened.getStatus().equals(context.getResources().getString(R.string.active))){
                    activeCases += 1;
                    totalCases += 1;
                }
                if (hasScreened.getStatus().equals(context.getResources().getString(R.string.recovered))){
                    recoveredCases += 1;
                    totalCases += 1;
                }
                if (hasScreened.getStatus().equals(context.getResources().getString(R.string.dead))){
                    deadCases += 1;
                    totalCases += 1;
                }
            }
        }
        updateUi();
        setupCharts();
    }

    private void updateUi() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String tempPositive = totalCases+"";
                statsPositive.startAnimation(AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
                statsPositive.setText(tempPositive);

                String tempActive = activeCases+"";
                statsActive.startAnimation(AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
                statsActive.setText(tempActive);

                String tempDead = deadCases+"";
                statsDead.startAnimation(AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
                statsDead.setText(tempDead);

                String tempRecovered = recoveredCases+"";
                statsRecovered.startAnimation(AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
                statsRecovered.setText(tempRecovered);
            }
        });
    }

    private void setupCharts(){
        ArrayList<PieEntry> cases = new ArrayList<>();
        cases.add(new PieEntry(totalCases, "Total"));
        cases.add(new PieEntry(activeCases, "Active"));
        cases.add(new PieEntry(recoveredCases, "Recovered"));
        cases.add(new PieEntry(deadCases, "Dead"));
        PieDataSet pieDataSet = new PieDataSet(cases, "Cas");
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTextSize(12f);
        pieDataSet.setColors(0xFF424242, 0xFFFF7300, 0xFF22B531, 0xFFD10F0F);
        PieData pieData = new PieData(pieDataSet);
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//            }
//        });
        pieChart.setData(pieData);
        pieChart.getDescription().setText("Progression");
        pieChart.animate();
        pieChart.invalidate();

        ArrayList<BarEntry> barCases = new ArrayList<>();
        final int[] colors = {0xFFD4D4D4, 0xFFC2C2C2, 0xFFB8B8B8, 0xFFA3A3A3, 0xFF959595, 0xFF888888, 0xFF707070, 0xFF636363, 0xFF595959, 0xFF545454};
        for (int i=0; i<regionValues.length; i++) {
            barCases.add(new BarEntry(i, regionValues[i]));
        }
        BarDataSet barDataSet = new BarDataSet(barCases, "Regions");
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(false);
        xAxis.setLabelCount(10);
        xAxis.setLabelRotationAngle(-15f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return regions[(int) value];
            }
        });
        YAxis yAxis = barChart.getAxisRight();
        yAxis.setDrawLabels(false);
        yAxis.setDrawGridLines(false);
        yAxis.setGranularity(1);
        yAxis.setCenterAxisLabels(true);
        yAxis.setGranularityEnabled(false);
        yAxis.setDrawZeroLine(false);
        yAxis.setDrawAxisLine(false);
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(maxValue);
        barDataSet.setColors(colors);
        barDataSet.setValueTextColor(0xFF000000);
        barDataSet.setValueTextSize(12f);
        BarData barData = new BarData(barDataSet);
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//            }
//        });
        barChart.setData(barData);
        barChart.getDescription().setText("");
        barChart.animateY( 1000);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setDrawBorders(false);
        barChart.setDrawingCacheEnabled(true);
        barChart.setFitBars(true);
        barChart.invalidate();
    }

    void initView(View view){
        statisticScrollView = view.findViewById(R.id.statisticScrollView);
        // setup pie char
        pieChart = (PieChart) view.findViewById(R.id.pieChart);
        // setup bar char
        barChart = (HorizontalBarChart) view.findViewById(R.id.barChart);

        chartContainer = (RelativeLayout) view.findViewById(R.id.chartContainer);

        statsPositive = (AppCompatTextView) view.findViewById(R.id.statsPositive);
        statsRecovered = (AppCompatTextView) view.findViewById(R.id.statsRecovered);
        statsDead = (AppCompatTextView) view.findViewById(R.id.statsDead);
        statsActive = (AppCompatTextView) view.findViewById(R.id.statsActive);
    }
}