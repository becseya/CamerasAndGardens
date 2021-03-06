package com.example.CamerasAndGardens;

import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.CamerasAndGardens.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, RadioGroup.OnCheckedChangeListener, GoogleMap.OnMapLongClickListener {

    static private final String INTENT_EXTRA_LAT = "LAT";
    static private final String INTENT_EXTRA_LON = "LON";
    static private final String INTENT_EXTRA_ZOOM = "ZOOM";
    static private final String INTENT_EXTRA_TITLE = "TITLE";

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private Circle myLastCircle = null;
    private FusedLocationProviderClient locationClient;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private Marker lastMarker = null;

    public static void appendExtraForMarker(Intent intent, LatLng coordinates, String title, boolean zoom) {
        intent.putExtra(INTENT_EXTRA_LAT, coordinates.latitude);
        intent.putExtra(INTENT_EXTRA_LON, coordinates.longitude);
        intent.putExtra(INTENT_EXTRA_TITLE, title);
        intent.putExtra(INTENT_EXTRA_ZOOM, zoom);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        RadioGroup radioGroup = findViewById(R.id.radioGroupMapType);
        radioGroup.setOnCheckedChangeListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // Callback for location permission request result
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        startLocationService();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();
        placeMarker(new LatLng(
                        intent.getDoubleExtra(INTENT_EXTRA_LAT, 0),
                        intent.getDoubleExtra(INTENT_EXTRA_LON, 0)
                ),
                intent.getStringExtra(INTENT_EXTRA_TITLE),
                intent.getBooleanExtra(INTENT_EXTRA_ZOOM, false));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.selMap:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.selSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.selHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.selTerrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            default:
                break;
        }
    }

    @Override
    public void onMapLongClick(LatLng coordinates) {
        if (myLastCircle != null)
            myLastCircle.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center(coordinates)    // LatLng point
                .radius(300 * 1000);    // In meters

        myLastCircle = mMap.addCircle(circleOptions);
    }

    public void onGoToLocation(View v) {
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            startLocationService();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationService() {
        // Request parameters
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Callback
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                handleLocation(locationResult.getLastLocation());
                locationClient.removeLocationUpdates(this);
            }
        };

        locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void handleLocation(Location loc) {
        updateMarker(new LatLng(loc.getLatitude(), loc.getLongitude()), "CURRENT POSITION", true);
    }

    private Marker placeMarker(LatLng coordinates, String title, boolean zoom) {
        Marker newMarker = mMap.addMarker(new MarkerOptions().position(coordinates).title(title));

        if (zoom) {
            mMap.moveCamera(CameraUpdateFactory.zoomTo(16.0f));
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates));

        return newMarker;
    }

    private void updateMarker(LatLng coordinates, String title, boolean zoom) {
        if (lastMarker != null) {
            lastMarker.remove();
        }

        lastMarker = placeMarker(coordinates, title, zoom);
    }
}