package com.example.nurmawaddah.position;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/* File Name: Indoor.java
 * Developer: Mawaddah Rahman
 *
 *  This is the indoor activity for the app, the user can train the location in this class
 *  We can add more building, follow the comments*/
public class Indoor extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap mMap;                         // variable to hold the map
    private LatLngBounds.Builder builder;           //  builder for the bounds of the map
    LatLng one, two, three, four;                   // 4 points of the building

    SensorManager manager;                          // variable to hold the sensor manager
    WifiManager wifiManager;                        // variable to hold wifi manager
    Sensor emfSensor;                               // variable to hold electromagnetic sensor
    float[] magneticfieldValues = new float[3];     // float array to hold the magnetic field
    int sensorAccuracy = 0;                         // int that store the sensor accuracy

    Circle[] locationpoints;                        // Array of circle that holds all of the reference points
    TextView mode;                                  // textview that notify whether the app is currently in training mode

    Spinner building;                               // drop downlist that holds all of the building
    final int KINGS_BUILDING = 0;                   // int to represents the kings building (currently just fleeming jenkins)
    final int MAIN_LIBRARY = 1;                     // int to represent the main library

    Database currentdatabase;                       // holds the current building's database
    Database kbdatabase;                            // holds the Fleeming Jenkins database
    Database mainlibrary;                           // holds the Main Library Database

    Marker currentLocation;                         // holds the marker of the current location
    double mindiameter = 0.0001;                    // sets the diameter of the circles for the building


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor);

        // get the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        // get the spinner
        building = findViewById(R.id.spinner);

        // set what would happpen when a building is selected from the list
        building.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                // set the map according to selection
                switch(position){
                    case KINGS_BUILDING:
                        setMap(KINGS_BUILDING);
                        break;
                    case MAIN_LIBRARY:
                        setMap(MAIN_LIBRARY);
                        break;
                    // add more building here
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // do nothing as we set the map to have the kings buildings as default

            }

        });



        //initilaise sensor manager
        manager = (SensorManager)getSystemService(getBaseContext().SENSOR_SERVICE);

        //initilaise emf sensor
        emfSensor = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //initialise wifimanager
        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // get the training textview
        mode = (TextView) findViewById(R.id.train);

        // initiate the database for the building
        kbdatabase = new Database(this, "PositioningData.db");
        mainlibrary = new Database(this, "PositioningData1.db");
        // add more building here
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // disable scrolling and zooming of the map, so it is easier to manipulate the indoor map
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);

        // set the kings building to be the default building at the start of the activity
        // and this would also create the circles
        setMap(KINGS_BUILDING);

        // register onClick listener to the circles
        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {

            @Override
            public void onCircleClick(Circle circle) {
                // get the train data for the circle when clicked
                trainData(circle);
            }
        });
    }

    // this method would draw the building and set the map, and all reference points required
    public void setMap(int position){
        // create the map bound builder
        builder = new LatLngBounds.Builder();

        // set the database and the 4 points of building according to the building selected
        switch(position){

            case KINGS_BUILDING:
                kingsBuilding();
                currentdatabase = kbdatabase;
                break;

            case MAIN_LIBRARY:
                mainLibrary();
                currentdatabase = mainlibrary;
                break;

            // add more buiding here
        }

        // set the markers to draw the building
        Marker markers[] = new Marker[4];
        markers[0] = mMap.addMarker(new MarkerOptions().position(one));
        markers[1] = mMap.addMarker(new MarkerOptions().position(two));
        markers[2] = mMap.addMarker(new MarkerOptions().position(three));
        markers[3] = mMap.addMarker(new MarkerOptions().position(four));

        // remove the markers
        mMap.clear();

        // include all of the markers in the building
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }

        // create a LatLng bounds using the builder
        LatLngBounds bounds = builder.build();
        int padding = 0; // offset from edges of the map in pixels

        // create a cameraupdate object using the bounds and move the camera
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,padding);
        mMap.moveCamera(cu);

        // draw the building bounds
        Polyline polygon = mMap.addPolyline(new PolylineOptions()
                .add(one, two, three, four, one));

        // call the method to draw the circles
        drawLocationPoints();

    }

    // method that draw the circles for the location reference points
    private void drawLocationPoints() {

        // calculate number of location
        // get length one two
        double xlength = Math.sqrt(Math.pow(one.latitude - two.latitude, 2) + Math.pow(one.longitude - two.longitude, 2));

        // get length one four
        double ylength = Math.sqrt(Math.pow(one.latitude - four.latitude, 2) + Math.pow(one.longitude - four.longitude, 2));

        // get number of points, depends on the mindiameter
        int x = (int) (xlength/mindiameter);
        int y = (int) (ylength/mindiameter);

        // get latlng for circles
        LatLng[] xpoints = new LatLng[x];
        LatLng[] ypoints = new LatLng[y];

        // get the LatLng of the Lat of the centre of the circles
        LatLng xdist = new LatLng(two.latitude - one.latitude, two.longitude - one.longitude);
        LatLng minxdist = new LatLng(xdist.latitude/(x*2.0),xdist.longitude/(x*2.0));
        for(int i = 0; i < x; i++) {
            xpoints[i] = new LatLng(minxdist.latitude*((i*2.0)+1), minxdist.longitude*((i*2.0)+1));
        }

        // get the LatLng of the Lng of the centre of the circles
        LatLng ydist = new LatLng(four.latitude - one.latitude, four.longitude - one.longitude);
        LatLng minydist = new LatLng(ydist.latitude/(y*2.0),ydist.longitude/(y*2.0));
        for(int i = 0; i < y; i++) {
            ypoints[i] = new LatLng(minydist.latitude*((i*2.0)+1), minydist.longitude*((i*2.0)+1));
        }

        // create an array of the LatLng of the circle and array of circles
        LatLng[] circleCentre = new LatLng[x*y];
        locationpoints = new Circle[x*y];
        int circleindex = 0;

        // set the LatLng centres of the circles
        for(int u = 0; u < y; u++){
            for (int v = 0; v < x; v++) {
                circleCentre[circleindex] = new LatLng(one.latitude + xpoints[v].latitude + ypoints[u].latitude,
                        one.longitude + xpoints[v].longitude + ypoints[u].longitude);
                circleindex++;
            }
        }

        // reset the circle index
        circleindex = 0;

        // draw location points
        for(LatLng centre: circleCentre) {
             locationpoints[circleindex] = mMap.addCircle(new CircleOptions()
                    .center(centre)
                    .radius(3)
                    .strokeColor(Color.TRANSPARENT)
                    .fillColor(Color.RED));
             circleindex++;
        }


        // check for trained data
        updateColor();

    }

    // checkl for trained data and recolor the circle accordingly
    private void updateColor() {
        for(Circle RP: locationpoints){
            // check whether the data is available
            if(currentdatabase.checkDataAvailable(RP.getCenter())) {
                // set the color to green when data is available
                RP.setFillColor(Color.GREEN);
            }
            else
                // set the color to red if not available
                RP.setFillColor(Color.RED);


        }

    }

    // this method set the 4 points for kings buildings
    private void kingsBuilding(){
        one = new LatLng(55.92267701, -3.17291244);
        two = new LatLng(55.922786726287, -3.172588907);
        three = new LatLng(55.922246441643, -3.1719398126);
        four = new LatLng(55.9221279011, -3.1722522899);

    }

    // this method sets the 4 points for the main library
    private void mainLibrary(){
        one = new LatLng(55.9425121, -3.18827044);
        two = new LatLng(55.943026567, -3.18851888);
        three = new LatLng(55.94282096, -3.18980734);
        four = new LatLng(55.942307627, -3.18954482);


    }

    // this method is for the 'Show Data' button
    public void showdata(View view) {

        // Set circle to be not clickable
        for (Circle circle: locationpoints){
            circle.setClickable(false);
        }

        // remove the current location marker if shown
        if(currentLocation  != null)
            currentLocation.remove();

        // hide the training textview
        mode.setVisibility(View.INVISIBLE);

        // create an alert dialog to show all of the data in the currentdatabase
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Data of current building");
        alertDialog.setMessage(currentdatabase.GetAllData());
        alertDialog.setCancelable(true);

        // add a delete all button and set the onClick method
        alertDialog.setNegativeButton("Delete All", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                currentdatabase.clearTable();
            }
        });

        // create the alert dialog
        AlertDialog alert = alertDialog.create();

        // show the alert dialog
        alert.show();

        // update the color of the circles
        updateColor();

    }

    // method that is called when the train button is clicked
    public void GetTrainingData(View view) {

        // remove the current location marker if available
        if(currentLocation  != null)
        currentLocation.remove();

        // set the train textview to be viisible
        mode.setVisibility(View.VISIBLE);

        // Toast to ask to pick on a circle
        Toast.makeText(this, "Pick a reference point to add data", Toast.LENGTH_SHORT).show();

        // Set circle to be clickable
        for (Circle circle: locationpoints){
            circle.setClickable(true);
        }

    }


    // this is the onClick method for the circle when it is clickable
    // it is used to collect data of the point
    private void trainData(Circle circle) {

        // get the latitude and longitude of the circle and store in xvalue and yvalue
        double xvalue = circle.getCenter().latitude;
        double yvalue =  circle.getCenter().longitude;

        // get the wifi and emf value
        String[] wifi = getWifi();
        double EMF = getEMF();

        // store the data as an entry in the database and get the success value
        int success = currentdatabase.insertData(xvalue, yvalue, wifi, EMF);

        // check whether data insertion is successful
        if (success < 0)
            // notify
            Toast.makeText(this, "try againnnn! " + success  , Toast.LENGTH_SHORT).show();
        else
            // notiify success
            Toast.makeText(this, "data stored!", Toast.LENGTH_SHORT).show();

        // update the color of the circle
        updateColor();
    }



    // method that is called when the bottom right button is clicked
    public void showIndoorLocation(View view) {

        // Set circle to be not clickable
        for (Circle circle: locationpoints){
            circle.setClickable(false);
        }

        // hide the train textview
        mode.setVisibility(View.INVISIBLE);

        // remove the current location marker if exist
        if(currentLocation  != null)
            currentLocation.remove();

        // get the 3 strongest wifi value
        String[] wifi = getWifi();

        // check whether there is a match to the strongest wifi value
        LatLng match = currentdatabase.PositionQuery(wifi[0], wifi[1], wifi[2]);

        if(match == null){
            // notify if match is not found
            Toast.makeText(this, "Indoor Location not found", Toast.LENGTH_SHORT).show();
        } else {
            // add a marker on the centre of the circle when the location is found
           currentLocation = mMap.addMarker(new MarkerOptions()
                   .position(match)
                   .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
    }

    // get the current EMF level
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // variable for the low pass filter
        float alpha = 0.95f;

        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            // only update the sensor value when sensor is high accuracy
            if(sensorAccuracy >= 2) {
                for (int i = 0; i < 3; i++) {
                    magneticfieldValues[i] = alpha * magneticfieldValues[i] + (1.0f - alpha) * sensorEvent.values[i];
                }
            }
        }
    }

    // get the accuracy number (0 - 3), where 0 is most noisy and 3 is most accurate
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        if(sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            sensorAccuracy = i;
        }
    }

    // this method would return an array of string that contains the wifi name, BSSID and level
    private String[] getWifi() {
        // initiate an empty string
        String[] list3wifi = new String[9];

        // create a comparator to help sorting the wifi list according to the level
        Comparator<ScanResult> comparator = new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                return (lhs.level >rhs.level ? -1 : (lhs.level==rhs.level ? 0 : 1));
            }
        };

        // get the list of available wifi and then sort with the comparator
        List<ScanResult> wifiScanList = wifiManager.getScanResults();
        Collections.sort(wifiScanList, comparator);

        // check whether the wifi is enabled and notify when its not
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if (!wifiEnabled) {
            Toast.makeText(this, "Wifi not enabled", Toast.LENGTH_SHORT).show();

        }

        //get the number of networks detected since there is no point in inserting values in the currentdatabase if there are no scans
        int wifiResultsCount = wifiScanList.size();

        // do this when there is data
        if (wifiResultsCount > 0) {

            // set the SSID BSSID and levels arrays to the right lengths (length of the results)
            String[] wifiSSID = new String[wifiResultsCount];
            String[] wifiBSSID = new String[wifiResultsCount];
            String[] wifiLevels = new String[wifiResultsCount];

            //store all values in the correct position
            for (int i = 0; i < wifiScanList.size(); i++) {
                wifiSSID[i] = wifiScanList.get(i).SSID;
                wifiBSSID[i] = wifiScanList.get(i).BSSID;
                //returns the absolute value of the signal to up to 10 levels of strength
                wifiLevels[i] = String.valueOf(wifiManager.calculateSignalLevel(wifiScanList.get(i).level, 10));

            }

            //due to the first result sorted to the highest signal strength level, this is saved for the querying later
            String currentSSID = wifiScanList.get(0).SSID;
            String currentBSSID = wifiScanList.get(0).BSSID;
            String currentLevel =   wifiLevels[0];

            // set the 3 most strong signal to the return array
            list3wifi[0] = currentSSID;
            list3wifi[1] = currentBSSID;
            list3wifi[2] = currentLevel;
            list3wifi[3] = wifiSSID[1];
            list3wifi[4] = wifiBSSID[1];
            list3wifi[5] = wifiLevels[1];
            list3wifi[6] = wifiSSID[2];
            list3wifi[7] = wifiBSSID[2];
            list3wifi[8] = wifiLevels[2];


        } else{
            // notify when there are no data
            Toast.makeText(this, "no wifi data", Toast.LENGTH_SHORT).show();
        }

        return list3wifi;
    }

    // get the current EMF value
    public double getEMF() {
        return Math.sqrt(Math.pow(magneticfieldValues[0],2)+
                Math.pow(magneticfieldValues[1],2)+ Math.pow(magneticfieldValues[2],2));

    }
}
