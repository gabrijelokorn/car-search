package com.example.car_search;

import android.location.Location;
import android.location.LocationListener;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

/*
* Class Locationer serves as a class to get locational information about user's device.
* */
public class Locationer implements LocationListener {

    private MainActivity mainActivity;

    public Locationer(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        mainActivity.update_device_location(longitude, latitude);
    }

    /*
    * When location is turned on we don't want an app to exit so onProviderEnabled function is overridden to do nothing.
    * */
    @Override
    public void onProviderEnabled(String string) {
        Toast.makeText(mainActivity.getApplicationContext(), "GPS was turned on.", Toast.LENGTH_SHORT).show();
    }

    /*
    * Method onProviderDisabled is overriden to not exit application when turning off device's locaiton.
    * It is used to once again start periodically start checking and waiting for the location service.
    * */
    @Override
    public void onProviderDisabled(String string) {
        this.mainActivity.disable_location_service();
        Toast.makeText(mainActivity.getApplicationContext(), "GPS was turned off.", Toast.LENGTH_SHORT).show();
    }
}
