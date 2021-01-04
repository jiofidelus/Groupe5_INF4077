package com.example.covid_app.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.example.covid_app.R;

public class GoogleMapFragment extends Fragment {

    Context context;
    AppCompatActivity activity;

    WebView mapView;
    private Dialog dialog;
    private LottieAnimationView loadingAnimation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        activity = (AppCompatActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_google_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    void initView(){
        loadingAnimation = activity.findViewById(R.id.loadingAnimation);
        mapView = activity.findViewById(R.id.mapView);

        WebSettings webSettings = mapView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mapView.loadUrl("https://www.google.cm/maps");
        showLoadingDialog();
        mapView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                hideLoadingDialog();
            }
        });
    }

    void showLoadingDialog(){
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_loading_dialog);
        loadingAnimation = dialog.findViewById(R.id.loadingAnimation);
        loadingAnimation.playAnimation();
        dialog.show();
    }

    void hideLoadingDialog(){
        if (dialog != null){
            loadingAnimation = dialog.findViewById(R.id.loadingAnimation);
            loadingAnimation.pauseAnimation();
            dialog.dismiss();
        }
    }
}