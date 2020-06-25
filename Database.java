package com.example.nurmawaddah.position;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import static android.content.ContentValues.TAG;

/* File Name: Database.java
*  Developer: Mawaddah Rahman
*  This file is the database class that creates database objects*/
public class Database extends SQLiteOpenHelper {

    // table name
    public static final String TABLE_NAME = "Data";

    //lattitude and longitude
    public static final String COLUMN_NAME_COORDINATEX = "CoordinateX";
    public static final String COLUMN_NAME_COORDINATEY = "CoordinateY";

    //wifi values
    public static final String COLUMN_NAME_WIFIONE_SSID = "WifiOneSSID";
    public static final String COLUMN_NAME_WIFIONE_BSSID = "WifiOneBSSID";
    public static final String COLUMN_NAME_WIFIONE_LEVEL = "WifiOneLevel";


    //wifi values
    public static final String COLUMN_NAME_WIFITWO_SSID = "WifiTwoSSID";
    public static final String COLUMN_NAME_WIFITWO_BSSID = "WifiTwoBSSID";
    public static final String COLUMN_NAME_WIFITWO_LEVEL = "WifiTwoLevel";

    //wifi values
    public static final String COLUMN_NAME_WIFITHR_SSID = "WifiThrSSID";
    public static final String COLUMN_NAME_WIFITHR_BSSID = "WifiThrBSSID";
    public static final String COLUMN_NAME_WIFITHR_LEVEL = "WifiThrLevel";

    // emf value
    public static final String COLUMN_NAME_EMF_VALUE = "Emf";

    //types of data types for the creation command
    public static final String TEXT_TYPE = " TEXT,";
    public static final String AND = " AND ";
    public static final String OR = " OR ";

    //string to create table
    public final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAME_COORDINATEX + TEXT_TYPE +
                    COLUMN_NAME_COORDINATEY  +    TEXT_TYPE +
                    COLUMN_NAME_WIFIONE_SSID  + TEXT_TYPE +
                    COLUMN_NAME_WIFIONE_BSSID  + TEXT_TYPE +
                    COLUMN_NAME_WIFIONE_LEVEL + TEXT_TYPE +
                    COLUMN_NAME_WIFITWO_SSID   + TEXT_TYPE +
                    COLUMN_NAME_WIFITWO_BSSID  + TEXT_TYPE +
                    COLUMN_NAME_WIFITWO_LEVEL + TEXT_TYPE +
                    COLUMN_NAME_WIFITHR_SSID   + TEXT_TYPE +
                    COLUMN_NAME_WIFITHR_BSSID  + TEXT_TYPE +
                    COLUMN_NAME_WIFITHR_LEVEL + TEXT_TYPE +
                    COLUMN_NAME_EMF_VALUE + " TEXT);";

    // create the database
    public Database(Context context, String databasename) {
        super(context,  databasename, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase database) {

        //create the table
        database.execSQL(SQL_CREATE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    //method for inserting all dat, including the latitude, longitude, wifi and emf values
    public int insertData(double latitude ,double longitude, String[] allWifi, double EMFvalue) {

        //get the writable database with and store content values
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        //put in the latitude and longitude as well
        values.put(COLUMN_NAME_COORDINATEX, String.valueOf(latitude));
        values.put(COLUMN_NAME_COORDINATEY, String.valueOf(longitude));

        //put in the wifi data and insert them into the table
        values.put(COLUMN_NAME_WIFIONE_SSID, allWifi[0]);
        values.put(COLUMN_NAME_WIFIONE_BSSID, allWifi[1]);
        values.put(COLUMN_NAME_WIFIONE_LEVEL, allWifi[2]);

        values.put(COLUMN_NAME_WIFITWO_SSID, allWifi[3]);
        values.put(COLUMN_NAME_WIFITWO_BSSID, allWifi[4]);
        values.put(COLUMN_NAME_WIFITWO_LEVEL, allWifi[5]);

        values.put(COLUMN_NAME_WIFITHR_SSID, allWifi[6]);
        values.put(COLUMN_NAME_WIFITHR_BSSID, allWifi[7]);
        values.put(COLUMN_NAME_WIFITHR_LEVEL, allWifi[8]);

        // put all of the data in the values
        values.put(COLUMN_NAME_EMF_VALUE, String.valueOf(EMFvalue));

        // insert teh value as a new row in the table and get the output
        long result = db.insert(TABLE_NAME,null, values);

        return (int) result;

    }

    //method to get all the data
    public String GetAllData() {
        // get the database table
        SQLiteDatabase db = this.getReadableDatabase();

        // initiate the string to hold all of the value and put in the table name
        String tableString = String.format("Table %s:\n", TABLE_NAME);

        // query all of the rows and store
        Cursor allRows  = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        // move the cursor to the start of the data retrieved and get the data
        if (allRows.moveToFirst() ){

            // get all of the column names and store in a string array
            String[] columnNames = allRows.getColumnNames();

            // go through the whole data and add the rows in the string
            do {
                // go through the whole row by the columns
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }

        return tableString;
    }




    //method to get the first match of the entries with the same wifi data
    public LatLng PositionQuery(String SSID, String BSSID, String level){
        //get the writable database
        SQLiteDatabase db = this.getWritableDatabase();

        //string denoting the columns to be selected which are the other columns of the table minus the wifi data
        String[] columns = {COLUMN_NAME_COORDINATEX, COLUMN_NAME_COORDINATEY};

        // First, try to find the match of the first wifi entry that is stored
        //the select statement showing the wifi column values to be compared
        String select = COLUMN_NAME_WIFIONE_SSID + "=? "  + AND + COLUMN_NAME_WIFIONE_BSSID + "=?" + AND
                + COLUMN_NAME_WIFIONE_LEVEL + "=?";

        //the arguments to compare the columns against (the WHERE statement)
        String[] where = {SSID, BSSID, level};

        //query the data
        Cursor resource = db.query(TABLE_NAME,columns, select, where, null, null, null );

        // only return if there is match
        if(resource!=null && resource.getCount()>0)
            return getLatLng(resource);


        // check for the second strongest signal if data not found for the first one
        //the select statment showing the wifi column values to be compared
        select = COLUMN_NAME_WIFITWO_SSID + "=? "  + AND + COLUMN_NAME_WIFITWO_BSSID + "=?" + AND
                + COLUMN_NAME_WIFITWO_LEVEL + "=?";

        //order it by accending wifi levels
        resource = db.query(TABLE_NAME,columns, select, where, null, null, null );

        // only return if data is found
        if(resource!=null && resource.getCount()>0) {
            return getLatLng(resource);
        }

        // check for the third strongest signal if data is still not found
        //the select statment showing the wifi column values to be compared
        select = COLUMN_NAME_WIFITHR_SSID + "=? "  + AND + COLUMN_NAME_WIFITHR_BSSID + "=?" + AND
                + COLUMN_NAME_WIFITHR_LEVEL + "=?";

        //query the data
        resource = db.query(TABLE_NAME,columns, select, where, null, null, null );

        // only return if data is found
        if(resource!=null && resource.getCount()>0) {
            return getLatLng(resource);
        }


        // check for the  strongest signal without the level if data cant be found
        //the select statment showing the wifi column values to be compared
        select = COLUMN_NAME_WIFITWO_SSID + "=? "  + AND + COLUMN_NAME_WIFITWO_BSSID + "=?" + OR
                + COLUMN_NAME_WIFITWO_LEVEL + "=?";

        //query the data
        resource = db.query(TABLE_NAME,columns, select, where, null, null, null );

        if(resource!=null && resource.getCount()>0)
            return getLatLng(resource);

        return getLatLng(resource);
    }

    // get the LatLng value from the list passed from the location query
    private LatLng getLatLng(Cursor resource) {
        LatLng match;

        // move cursor to the first row and get the LatLng value
        if (resource.moveToFirst() ){
            match = new LatLng(Double.parseDouble(resource.getString(resource.getColumnIndex(COLUMN_NAME_COORDINATEX))),
                    Double.parseDouble(resource.getString(resource.getColumnIndex(COLUMN_NAME_COORDINATEY))));

        } else {
            // set match to null if there is no data
            match = null;
        }

        return match;
    }

    //method that check whether the data exists in the database
    public boolean checkDataAvailable(LatLng circleCentre){
        //get a writable database
        SQLiteDatabase db = this.getWritableDatabase();

        //string denoting the columns to be selected which are any column
        String[] columns = {COLUMN_NAME_EMF_VALUE};

        //the select statement showing the wifi column values to be compared
        String select = COLUMN_NAME_COORDINATEX + "=? "  + AND + COLUMN_NAME_COORDINATEY + "=?";

        //the arguments to compare the columns against (the WHERE statement)
        String[] where = {String.valueOf(circleCentre.latitude), String.valueOf(circleCentre.longitude)};

        // query the data
        Cursor resource = db.query(TABLE_NAME,columns, select, where, null, null, null );

        // if there is data return true
        if( resource.getCount()>0)
            return true;

        // else return false
        return false;

    }

    // method to clear all of the data in the table
    public void clearTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME);
    }

}
