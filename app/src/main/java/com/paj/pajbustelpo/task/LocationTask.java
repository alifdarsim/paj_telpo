package com.paj.pajbustelpo.task;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.paj.pajbustelpo.DatabaseHelper;
import com.paj.pajbustelpo.Helper;
import com.paj.pajbustelpo.activities.MainActivity;

//custom helper class just for paj telpo use
public class LocationTask {

    MainActivity mainActivity;
    DatabaseHelper db;
    private double currentLat, currentLng;
    private double cumulativeDisplacement = 0;
    private int cumulativeTime = 0;

    public LocationTask(MainActivity context, DatabaseHelper db) {
        this.mainActivity = context;
        this.db = db;
    }

    public void startRequestLocation(Context context, OnGetLocationUpdate listener){
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setSmallestDisplacement(0);
        LocationCallback mLocationCallback = new LocationCallback() {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
//                Log.e("tasklocation", "onLocationResult: " + "11111111111111");
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {

                        //get location data from satellite
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        double speed = location.getSpeed();
                        double bearing = location.getBearing();
                        long gpstime = location.getTime();
                        double accuracy = location.getAccuracy();

//                        if (accuracy > 30) {
//                            mainActivity.logger.writeToLogger("Accuracy is low ("+accuracy+"m). Ignoring GPS data", "yellow");
//                            return;
//                        }

                        //get displacement from previous location
                        double displacement = Helper.getDisplacement(currentLat, currentLng, latitude, longitude);
                        currentLat = latitude;
                        currentLng = longitude;

                        //collect the cumulative distance only when displacement is more than 1m
                        if (displacement > 1) cumulativeDisplacement = cumulativeDisplacement + displacement;

                        //log the location data
                        logLocationData(latitude, longitude, speed, bearing, gpstime, accuracy, displacement, cumulativeDisplacement);

//                        if (cumulativeDisplacement > 40.0){
//                            mainActivity.logger.writeToLogger("Insert location into sqlite | Lat: " + latitude + ", Lng: " + longitude, "green");
//                            insertIntoDataBase(latitude, longitude, speed, bearing, gpstime, accuracy);
//                        }

                        cumulativeTime++;
                        if (cumulativeTime > 4){
                            mainActivity.logger.writeToLogger("Insert location into sqlite | Lat: " + latitude + ", Lng: " + longitude, "green");
                            if (speed > 1) insertIntoDataBase(latitude, longitude, speed, bearing, gpstime, accuracy);
                            cumulativeTime = 0;
                        }

//                        mainActivity.logger.writeToLogger("Insert location into sqlite | Lat: " + latitude + ", Lng: " + longitude, "green");
//                        insertIntoDataBase(latitude, longitude, speed, bearing, gpstime, accuracy);


                        //callback data to mainActivity if needed to do something
                        listener.locationUpdate(latitude, longitude, speed, bearing, gpstime, accuracy, displacement);
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(context).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void logLocationData(double latitude, double longitude, double speed, double bearing, long epoch, double accuracy, double displacement, double cumulativeDisplacement){
        String str_latitude = String.format("%.7f", latitude);
        String str_longitude = String.format("%.7f", longitude);
        String str_speed = String.format("%.2f", speed);
        String str_bearing = String.format("%.0f", bearing);
        String str_epoch = Helper.epochToDate(epoch);
        String str_accuracy = String.format("%.1f", accuracy);
        String str_displacement = String.format("%.1f", displacement);
        String str_cum_displacement = String.format("%.1f", cumulativeDisplacement);
        String strAppend = "Lat: " + str_latitude + " | Lng: " + str_longitude + " | Spd: " + str_speed + " | Deg: " + str_bearing + " | Acc: " + str_accuracy + " | Disp: " + str_displacement+ " | TotalDisp: " + str_cum_displacement;
        mainActivity.logger.writeToLogger(strAppend, "gray");
    }

    private void insertIntoDataBase(double latitude, double longitude, double speed, double bearing, long epoch, double accuracy){
        //convert any necessary value
        String datetime = Helper.epochToDate(epoch);
        double speedKmh = Helper.msToKmh(speed);
        //insert into sqlite
        boolean isInserted = db.insertLocationData(latitude, longitude, speedKmh, bearing, datetime, accuracy);
        if (!isInserted) {
            Log.e("SQLite", "Failed to insert sqlite");
            Toast.makeText(mainActivity, "Failed to inset sqlite", Toast.LENGTH_SHORT).show();
        }
        else{
            Log.e("SQLite", "Success to insert sqlite");
            cumulativeDisplacement = 0.0;
        }
    }

    public interface OnGetLocationUpdate {
        void locationUpdate(double latitude, double longitude, double speed, double bearing, long time, double accuracy, double distance);
    }

}
