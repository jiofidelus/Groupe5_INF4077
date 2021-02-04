package com.example.covid_app.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.example.covid_app.R;
import com.example.covid_app.models.PositionModel;
import com.example.covid_app.models.SensibilisationMessage;
import com.example.covid_app.models.SmsModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class MapFragment extends Fragment implements OnMapReadyCallback, PermissionsListener {

    Context context;
    AppCompatActivity activity;
    private FirebaseFirestore db;

    MapView mapView;
    private MapboxMap mapboxMap;

    SwitchCompat shareMyPositionSwitch;

    // variables for adding location layer
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    LocationComponentActivationOptions locationComponentActivationOptions;

    private int AUTOCOMPLETE_REQUEST_CODE = 1;
    private int LOCATION_PERMISSION_CODE = 21;
    private String TAG = "covid_app";
    private int requestCode;
    private String[] permissions;
    private int[] grantResults;
    private LottieAnimationView topLoadingAnimation;
    private boolean canRunThread;
    private ArrayList<PositionModel> positionsList = new ArrayList<>();
    private String userUID = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token));
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_map, container, false);


        context = result.getContext();
        activity = (AppCompatActivity) context;

        initView(result);
        mapView.onCreate(savedInstanceState);
        canRunThread = true;
        stopLocationsPositions();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (canRunThread){
                    checkInteractions();
                }
            }
        }).start();

        return result;
    }

    private void checkInteractions() {
        shareMyPositionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    checkLocationPermissions();
                    getLocationsPositions();
                }else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapView.setVisibility(View.INVISIBLE);
                        }
                    });
                    stopLocationsPositions();
                }
            }
        });
    }

    private void updateMyPosition(PositionModel positionModel){
        db.collection("positions")
                .whereEqualTo("uid", userUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            String newUid = null;
                            for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())){
                                newUid = documentSnapshot.getId();
                            }
                            if (newUid != null){
                                db.collection("positions")
                                        .document(newUid)
                                        .update("position", positionModel.getUid())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                            }
                                        });
                            }else {
                                db.collection("positions").add(positionModel)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                //
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void getLocationsPositions() {
        getUid();
        db.enableNetwork();
        db.collection("positions").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                positionsList.clear();
                positionsList = new ArrayList<>();
                for (DocumentSnapshot doc : value) {
                    PositionModel positionModel = doc.toObject(PositionModel.class);
                    if (positionModel != null){
                        if(!positionModel.getUid().equals(userUID)){
                            positionsList.add(positionModel);
                        }
                    }
                }
                updatePositions();
            }
        });
    }

    private void getUid(){
        try {
            userUID = Settings.Secure.getString(
                    activity.getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePositions() {
        for (int i=0; i<positionsList.size(); i++){
            addOthersDestinations(positionsList.get(i));
        }
    }

    private void stopLocationsPositions(){
        db.disableNetwork();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (context != null){
            if (requestCode == LOCATION_PERMISSION_CODE) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Permissions acceptees", Toast.LENGTH_SHORT).show();
                    checkLocationPermissions();
                } else {
                    Toast.makeText(context, "Permissions refusees", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void checkLocationPermissions() {
        if (context != null){
            if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);
            } else {
                getMap();
            }
        }
    }

    private void getMap() {
        if (context != null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapView.setVisibility(View.VISIBLE);
                }
            });
            mapView.getMapAsync(this);
        }
    }

    void initView(View view) {
        mapView = view.findViewById(R.id.mapView);
        topLoadingAnimation = view.findViewById(R.id.topLoadingAnimation);
        shareMyPositionSwitch = view.findViewById(R.id.shareMyPositionSwitch);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (context != null){
            if (PermissionsManager.areLocationPermissionsGranted(context)) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(context)
                        .trackingGesturesManagement(true)
                        .accuracyColor(ContextCompat.getColor(context, R.color.green))
                        .build();
                locationComponentActivationOptions = LocationComponentActivationOptions.builder(context, loadedMapStyle)
                        .locationComponentOptions(customLocationComponentOptions)
                        .build();
                locationComponent = mapboxMap.getLocationComponent();
                locationComponent.activateLocationComponent(locationComponentActivationOptions);
                locationComponent.setLocationComponentEnabled(true);
                locationComponent.setCameraMode(CameraMode.TRACKING);
            } else {
                permissionsManager = new PermissionsManager(this);
                permissionsManager.requestLocationPermissions(activity);
            }
        }
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        if(context != null){
            this.mapboxMap = mapboxMap;
            mapboxMap.setStyle(getString(R.string.mapbox_style_mapbox_streets), new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                    addDestinationIconSymbolLayer(style);
                }
            });
        }
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        if (context != null){
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.mapbox_marker_icon_default, null);
            Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
            loadedMapStyle.addImage("destination-icon-id",
                    mBitmap);
            GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
            loadedMapStyle.addSource(geoJsonSource);
            SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
            destinationSymbolLayer.withProperties(
                    iconImage("destination-icon-id"),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true)
            );
            loadedMapStyle.addLayer(destinationSymbolLayer);
        }
    }

    private void addOthersDestinations(PositionModel positionModel){
        //
    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //
    }

    @Override
    public void onPermissionResult(boolean granted) {
        //
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        context = null;
        canRunThread = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
        canRunThread = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        context = null;
        canRunThread = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        context = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        context = getContext();
    }

    @Override
    public void onStart() {
        super.onStart();
        context = getContext();
    }

    void showTopLoadingDialog(){
        topLoadingAnimation.playAnimation();
        topLoadingAnimation.setVisibility(View.VISIBLE);
    }

    void hideTopLoadingDialog(){
        topLoadingAnimation.pauseAnimation();
        topLoadingAnimation.setVisibility(View.INVISIBLE);
    }

}