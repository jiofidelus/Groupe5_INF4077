package com.example.covid_app.fragments;

import android.app.Activity;
import android.content.Context;
<<<<<<< HEAD
=======
import android.graphics.Color;
>>>>>>> UI statistics update
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.example.covid_app.R;
<<<<<<< HEAD
=======
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
>>>>>>> UI statistics update

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
<<<<<<< HEAD

        getViews();
        OverScrollDecoratorHelper.setUpOverScroll(statisticScrollView);
=======
        getViews();
        OverScrollDecoratorHelper.setUpOverScroll(statisticScrollView);


        // setup pie char
        PieChart pieChart = (PieChart) activity.findViewById(R.id.pieChart);
        ArrayList<PieEntry> cases = new ArrayList<>();
        cases.add(new PieEntry(1223, "Total"));
        cases.add(new PieEntry(783, "Active"));
        cases.add(new PieEntry(187, "Recovered"));
        cases.add(new PieEntry(31, "Dead"));
        PieDataSet pieDataSet = new PieDataSet(cases, "Cas");
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTextSize(12f);
        pieDataSet.setColors(new int[] {0xFF424242, 0xFFFF7300, 0xFF22B531, 0xFFD10F0F});
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setText("Progression");
        pieChart.animate();

        // setup bar char
        HorizontalBarChart barChart = (HorizontalBarChart) activity.findViewById(R.id.barChart);
        ArrayList<BarEntry> barCses = new ArrayList<>();
        final float[] values = {10, 123, 9, 98, 47, 743, 78, 90, 12, 32};
        final String regions[] = {"Centre ","Adamaoua","Est","Sud", "Nord", "Extreme-nord", "Ouest", "Nord-ouest", "Littoral", "Sud-ouest"};
        final int[] colors = {0xFFD4D4D4, 0xFFC2C2C2, 0xFFB8B8B8, 0xFFA3A3A3, 0xFF959595, 0xFF888888, 0xFF707070, 0xFF636363, 0xFF595959, 0xFF545454};
        Arrays.sort(values);
        for (int i=0; i<values.length; i++) {
            barCses.add(new BarEntry(i, values[i]));
        }
        BarDataSet barDataSet = new BarDataSet(barCses, "Regions");
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
        barDataSet.setColors(colors);
        barDataSet.setValueTextColor(0xFF000000);
        barDataSet.setValueTextSize(12f);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.getDescription().setText("");
        barChart.animateY( 1000);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setDrawBorders(false);
        barChart.setDrawingCacheEnabled(true);
        barChart.setFitBars(true);
        barChart.invalidate();
>>>>>>> UI statistics update
    }

    void getViews(){
        statisticScrollView = activity.findViewById(R.id.statisticScrollView);
    }
}