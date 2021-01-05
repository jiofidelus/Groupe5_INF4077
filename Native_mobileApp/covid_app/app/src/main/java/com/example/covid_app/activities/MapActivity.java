package com.example.covid_app.activities;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.covid_app.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.location.DefaultLocationProvider;
import com.mapbox.search.ui.view.SearchBottomSheetView;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class MapActivity extends AppCompatActivity {

    ScrollView mapScrollVIew;
    EditText mapSource;
    EditText mapDestination;
    MapView mapView;
    SearchBottomSheetView searchBottom;

    String source = "";
    String destination = "";

    private int AUTOCOMPLETE_REQUEST_CODE = 1;
    private int LOCATION_PERMISSION_CODE = 21;
    private String TAG = "covid_app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(MapActivity.this, getString(R.string.mapbox_access_token));
        MapboxSearchSdk.initialize(
                new Application(),
                getString(R.string.mapbox_access_token),
                new DefaultLocationProvider(new Application())
        );
        setContentView(R.layout.activity_map);

        initView();
        mapView.onCreate(savedInstanceState);
        OverScrollDecoratorHelper.setUpOverScroll(mapScrollVIew);
        checkInteractions();
        checkLocationPermissions();
        searchBottom.initializeSearch(savedInstanceState, new SearchBottomSheetView.Configuration());

    }

    private void checkInteractions() {
        mapSource.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mapDestination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    private void checkLocationPermissions(){
        if (ContextCompat.checkSelfPermission(MapActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }else{
            //getMap();
        }
    }

    private void getMap(){
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.SATELLITE, new Style.OnStyleLoaded() {
                    @Override public void onStyleLoaded(@NonNull Style style) {

                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments.

                    }
                });
            }
        });
    }

    void initView(){
        mapScrollVIew = findViewById(R.id.mapScrollVIew);
        mapSource = findViewById(R.id.mapSource);
        mapDestination = findViewById(R.id.mapDestination);
        mapView = findViewById(R.id.mapView);
        searchBottom = findViewById(R.id.searchBottom);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }
}