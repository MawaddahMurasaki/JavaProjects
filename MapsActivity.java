package com.example.nurmawaddah.position;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/* File Name: MapsActivity.java
 * Developer: Mawaddah Rahman
 *
 *  This is the entry point of the application, which is the main activity of the
*  application. In this activity, the user can update the current position and
*  switch to indoor activity.*/

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Variable initiations

    // The map
    private GoogleMap mMap;

    // Initiate all variables needed for requesting the current location
    private FusedLocationProviderClient mFusedLocationProviderClient;
    LocationCallback mLocationCallback;
    SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    LocationSettingsRequest mLocationSettingsRequest;

    // The current location of the user
    Location currentlocation;

    // The textviews that holds the current latitude and logitude
    TextView currentLat;
    TextView currentLng;

    // The current location marker
    Marker currentMarker;

    // The boolean that tells whether the current location is shown
    private Boolean showCurrent = false;

    // The geocoder that decodes the name of the location
    Geocoder mGeocoder;

    // The registered buildings are storeed as polygons in this class
    // Add new buildings here.
    Polygon library;
    Polygon kings;


    // This method is executed when the map is successfully initiated
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Notify the user that the map is ready
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();

        // update the value of the global map variable
        mMap = googleMap;

        // store the location of edinburgh
        LatLng Edinburgh = new LatLng(55.9533, -3.1883);

        // move the map camera to Edinburgh at 10f zoom
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Edinburgh, 10f));

        // //////////////////////////////////////////////////////////////////
        /*// This is where the buildings are initiated, each building are defined
        *    using 4 location points on the map(or more).*/
         library = mMap.addPolygon(new PolygonOptions()
                .add(new LatLng(55.9425121, -3.18827044), new LatLng(55.943026567, -3.18851888), new LatLng(55.94282096, -3.18980734), new LatLng(55.942307627, -3.18954482))
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE));

         kings = mMap.addPolygon(new PolygonOptions()
                .add(new LatLng(55.92267701, -3.17291244), new LatLng(55.922786726287, -3.172588907), new LatLng(55.922246441643, -3.1719398126), new LatLng(55.9221279011, -3.1722522899))
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE));

         // add new building here
         // ///////////////////////////////////////////////////////////////////
    }

    // The entry point of the main activity.
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the view of the activity to activity_maps.xml
        setContentView(R.layout.activity_maps);

        // /////////////////////////////////////////////////////////////////////
        // check the permissions needed for the application before executing anything
        // for safety check

        // check for the ACCESS_FINE_LOCATION permission
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // check for the ACCESS_COARSE_LOCATION permission
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        // check for the WRITE_EXTERNAL_STORAGE permission
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        // check for the READ_EXTERNAL_STORAGE permission
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        // add more permission requests here
        // //////////////////////////////////////////////////////////////////////////////

        // get the map and store in the fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // get the textview by the id
        currentLat = findViewById(R.id.latitude);
        currentLng = findViewById(R.id.longitude);

        // initiate the geocoder
        mGeocoder = new Geocoder(this);

        // set up location provider
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // set up location setting client
        mSettingsClient = LocationServices.getSettingsClient(this);

        // set up current location request and callback
        createLocationCallback();
        buildLocationSettingsRequest();
        createLocationRequest();

        // update current location
        getLastLocation();



    }

    // This method is called to update the current location by a location callback
    private void createLocationCallback() {
        // execute a callback
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // update the current location
                currentlocation = locationResult.getLastLocation();
                updateLocation();
            }
        };
    }

    // This method is used to get the current location from the location provider.
    private void getLastLocation() {
        try {
            // add a complete listener to this method so that the method is executed
            // for everytime there is a complete listener by overriding the onComplete()
            mFusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            // update the current location only when the task is successful
                            if (task.isSuccessful() && task.getResult() != null) {
                                currentlocation = task.getResult();
                            } else {
                                // Notify
                                Toast.makeText(MapsActivity.this,  "Failed to get location.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Toast.makeText(this, "Lost location permission.", Toast.LENGTH_SHORT).show();

        }
    }

    // update the location Texviews with the current latitude and longitude
    private void updateLocation() {

        currentLat.setText("Latitude: " + currentlocation.getLatitude());
        currentLng.setText("Latitude: " + currentlocation.getLongitude());


    }

    // create location request and assign parameters
    @SuppressLint("RestrictedApi")
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This is inexact
        mLocationRequest.setInterval(10000);

        // Sets the fastest rate for active location updates. This interval is exact, and
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(5000);

        // set the location to be high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // this method builds location service request
    private void buildLocationSettingsRequest() {

        // create a builder for location setting request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);

        // build location setting request and make sure it is in the right context and correct setting
        mLocationSettingsRequest = builder.build();
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        // add onSuccess and onFailureListener, mainly to catch the error
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // do nothing if the location setting request is successful
                // this is because the update of current location is done
                // whenever there is a callback
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MapsActivity.this,99);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
            }
        });
    }



    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        // resume the location callback after the app was suspended
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // do nothing
    }

    // this method is called when the location button is pressed
    public void showLocation(View view) {

        // if no location is currently shown
        if(!showCurrent) {

            // get the current time
            String mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

            // put a marker on the map with the current location and add the current time as snippet
            currentMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(currentlocation.getLatitude(),
                    currentlocation.getLongitude())).title("You are here").snippet("Last updated on: " + mLastUpdateTime));

            // move camera to the current location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentlocation.getLatitude(), currentlocation.getLongitude()), 18f));

            // update the value
            showCurrent = true;

           // create an address list to decode the current location's address
           List<Address> matches = null;

            try {
                // get the addresses
                matches = mGeocoder.getFromLocation(currentlocation.getLatitude(), currentlocation.getLongitude(),1);
            } catch (IOException e) {
                // catch exception and notify if address is not obtained
                e.printStackTrace();
                Toast.makeText(this, "Cant get Address", Toast.LENGTH_SHORT).show();
            }

            // get the best match address
            Address bestMatch = (matches.isEmpty() ? null : matches.get(0));

            if(bestMatch != null)
                // notify the location to the user
                Toast.makeText(this, "You are at " + bestMatch.getFeatureName() , Toast.LENGTH_LONG).show();
            else
                // notify if no address is obtained
                Toast.makeText(this, "no Address obtained", Toast.LENGTH_SHORT).show();


        } else {
            // if there is currently a marker on the map, remove the marker
            currentMarker.remove();

            // then, update the variable
            showCurrent = false;
        }
    }


    // method when the building button is clicked
    public void indoorActivity(View view) {
        Intent intent = new Intent(this, Indoor.class);

        //start intent
        startActivity(intent);


    }
}

