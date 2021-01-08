package com.example.covid_app.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaRouter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;

import com.example.covid_app.R;
import com.google.android.gms.common.api.Response;
import com.google.android.material.internal.NavigationMenuPresenter;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.Utils;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.navigation.base.formatter.DistanceFormatter;
import com.mapbox.navigation.base.internal.route.RouteUrl;
import com.mapbox.navigation.base.options.DeviceProfile;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.options.OnboardRouterOptions;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.directions.session.RoutesRequestCallback;
import com.mapbox.navigation.ui.NavigationView;
import com.mapbox.navigation.ui.NavigationViewOptions;
import com.mapbox.navigation.ui.OnNavigationReadyCallback;
import com.mapbox.navigation.ui.route.NavigationMapRoute;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.mapbox.api.directions.v5.DirectionsCriteria.PROFILE_DRIVING_TRAFFIC;
import static com.mapbox.api.directions.v5.DirectionsCriteria.PROFILE_WALKING;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class MapFragment extends Fragment implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {

    Context context;
    AppCompatActivity activity;

    MapView mapView;
    private MapboxMap mapboxMap;

    RelativeLayout navigationButtonClick;
    CardView navigationButton;

    // variables for adding location layer
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    LocationComponentActivationOptions locationComponentActivationOptions;
    // variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    private NavigationMapRoute navigationMapRoute;
    MapboxNavigation mapboxNavigation;
    private NavigationView navigationView;

    private int AUTOCOMPLETE_REQUEST_CODE = 1;
    private int LOCATION_PERMISSION_CODE = 21;
    private String TAG = "covid_app";
    private int requestCode;
    private String[] permissions;
    private int[] grantResults;
    private RoutesRequestCallback routesReqCallback = new RoutesRequestCallback() {
        @Override
        public void onRoutesReady(@NotNull List<? extends DirectionsRoute> list) {
            if (list.isEmpty()) {
                Log.e(TAG, "No routes found");
                return;
            }
            currentRoute = list.get(0);
            navigationMapRoute.addRoute(currentRoute);
        }

        @Override
        public void onRoutesRequestFailure(@NotNull Throwable throwable, @NotNull RouteOptions routeOptions) {
            //
        }

        @Override
        public void onRoutesRequestCanceled(@NotNull RouteOptions routeOptions) {
            //
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        activity = (AppCompatActivity) context;
        Mapbox.getInstance(context, getString(R.string.mapbox_access_token));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        mapView.onCreate(savedInstanceState);
        checkInteractions();
        checkLocationPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Permissions acceptees", Toast.LENGTH_SHORT).show();
                checkLocationPermissions();
            } else {
                Toast.makeText(context, "Permissions refusees", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkInteractions() {
        navigationButtonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });
    }

    private void checkLocationPermissions() {
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

    private void getMap() {
        mapView.getMapAsync(this);
    }

    void initView() {
        mapView = activity.findViewById(R.id.mapView);
        navigationButton = activity.findViewById(R.id.navigationButton);
        navigationButtonClick = activity.findViewById(R.id.navigationButtonClick);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
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

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(getString(R.string.mapbox_navigation_guidance_day), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);

                addDestinationIconSymbolLayer(style);

                mapboxMap.addOnMapClickListener(MapFragment.this);
            }
        });
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
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

    @SuppressWarnings( {"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());

        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }

        getRoute(originPoint, destinationPoint);
        navigationButton.setAlpha(1);
        navigationButton.setVisibility(View.VISIBLE);
        return true;
    }

    private void getRoute(Point origin, Point destination) {
        ArrayList<Point> list = new ArrayList<>();
        list.add(origin);
        list.add(destination);
//        mapboxNavigation = new MapboxNavigation(new NavigationOptions(
//                context,
//                Mapbox.getAccessToken(),
//                ACCESS_FINE_LOCATION,
//                1,
//                1,
//                10,
//                new OnboardRouterOptions.Builder(),
//                true,
//                true,
//                new DeviceProfile.Builder()));
        mapboxNavigation.requestRoutes(
                RouteOptions.builder()
                        .accessToken(Mapbox.getAccessToken())
                        .coordinates(list)
                        .geometries(RouteUrl.GEOMETRY_POLYLINE6)
                        .profile(RouteUrl.PROFILE_DRIVING)
                        .baseUrl(RouteUrl.BASE_URL)
                        .requestUuid(PROFILE_WALKING)
                        .user(PROFILE_DRIVING_TRAFFIC)
                        .build(),
                routesReqCallback
        );
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //
    }

    @Override
    public void onPermissionResult(boolean granted) {
        //
    }
}