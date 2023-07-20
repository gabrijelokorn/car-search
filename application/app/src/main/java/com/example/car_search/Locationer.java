package com.example.car_search;

import android.location.Location;
import android.location.LocationListener;

import androidx.annotation.NonNull;

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
}
